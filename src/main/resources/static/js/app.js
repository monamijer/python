// js/app.js
// Vanilla JS, no build step — fetch() against the same-origin Spring Boot API.

const API = "/api";

// A tiny inline SVG used whenever a series/actor has no image — avoids a
// broken-image icon and gives screen readers something meaningful via alt text.
const IMAGE_PLACEHOLDER =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='300'%3E" +
  "%3Crect width='200' height='300' fill='%23ddd'/%3E" +
  "%3Ctext x='50%25' y='50%25' font-size='16' fill='%23888' text-anchor='middle' dy='.3em'%3EPas d'image%3C/text%3E" +
  "%3C/svg%3E";

// --- Auth / session storage ---

function getToken() { return localStorage.getItem("token"); }
function getUtilisateurId() { return localStorage.getItem("utilisateurId"); }
function getPseudo() { return localStorage.getItem("pseudo"); }

function setSession(token, utilisateurId, pseudo) {
  localStorage.setItem("token", token);
  localStorage.setItem("utilisateurId", utilisateurId);
  localStorage.setItem("pseudo", pseudo);
}

function deconnexion() {
  localStorage.removeItem("token");
  localStorage.removeItem("utilisateurId");
  localStorage.removeItem("pseudo");
  location.reload();
}

// --- Auth tabs ---

document.querySelectorAll(".auth-tab").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".auth-tab").forEach((b) => {
      b.classList.remove("active");
      b.setAttribute("aria-selected", "false");
    });
    document.querySelectorAll(".auth-panel").forEach((p) => p.setAttribute("hidden", ""));
    btn.classList.add("active");
    btn.setAttribute("aria-selected", "true");
    document.getElementById(`form-${btn.dataset.auth}`).removeAttribute("hidden");
    document.getElementById("login-erreur").textContent = "";
  });
});

// --- Generic API wrapper ---

async function appelApi(url, options = {}) {
  const token = getToken();
  const headers = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const reponse = await fetch(url, { headers, ...options });

  if (reponse.status === 401 || reponse.status === 403) {
    if (!url.includes("/auth/")) deconnexion();
    throw new Error("Non authentifié");
  }

  if (!reponse.ok) {
    const erreur = await reponse.json().catch(() => ({ message: reponse.statusText }));
    throw new Error(erreur.message || "Erreur API");
  }
  return reponse.status === 204 ? null : reponse.json();
}

function creerElement(html) {
  const conteneur = document.createElement("div");
  conteneur.innerHTML = html.trim();
  return conteneur.firstElementChild;
}

// Shared empty-state renderer — every list in the app uses this instead of
// silently rendering nothing, per the "no silent empty state" requirement.
function messageVide(texte) {
  return `<p class="aucun-resultat" role="status">${texte}</p>`;
}

// --- Login / Inscription ---

function afficherLogin() {
  document.getElementById("app").classList.add("hidden");
  document.getElementById("login").classList.remove("hidden");
}

function afficherApp(pseudo) {
  document.getElementById("login").classList.add("hidden");
  document.getElementById("app").classList.remove("hidden");
  if (pseudo) document.getElementById("nom-utilisateur").textContent = `👤 ${pseudo}`;
  chargerMesSeries();
  chargerGenres();
}

document.getElementById("form-connexion").addEventListener("submit", async (e) => {
  e.preventDefault();
  const email = document.getElementById("login-email").value;
  const motDePasse = document.getElementById("login-motdepasse").value;
  try {
    const data = await appelApi(`${API}/auth/connexion`, {
      method: "POST",
      body: JSON.stringify({ email, motDePasse }),
    });
    setSession(data.token, data.utilisateurId, data.pseudo);
    afficherApp(data.pseudo);
  } catch (err) {
    document.getElementById("login-erreur").textContent = "Identifiants invalides";
  }
});

document.getElementById("form-inscription").addEventListener("submit", async (e) => {
  e.preventDefault();
  const pseudo = document.getElementById("inscription-pseudo").value;
  const email = document.getElementById("inscription-email").value;
  const motDePasse = document.getElementById("inscription-motdepasse").value;
  try {
    const data = await appelApi(`${API}/auth/inscription`, {
      method: "POST",
      body: JSON.stringify({ pseudo, email, motDePasse }),
    });
    setSession(data.token, data.utilisateurId, data.pseudo);
    afficherApp(data.pseudo);
  } catch (err) {
    document.getElementById("login-erreur").textContent = err.message;
  }
});

document.getElementById("btn-deconnexion")?.addEventListener("click", deconnexion);

// --- Main tabs ---

document.querySelectorAll(".tab-btn").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tab-btn").forEach((b) => {
      b.classList.remove("active");
      b.setAttribute("aria-selected", "false");
    });
    document.querySelectorAll(".tab-panel").forEach((p) => p.setAttribute("hidden", ""));
    btn.classList.add("active");
    btn.setAttribute("aria-selected", "true");
    document.getElementById(btn.dataset.tab).removeAttribute("hidden");
  });
});

// --- My series: load, sort, filter ---

let seriesEnMemoire = []; // cache of {serie, progression} so sort/filter don't refetch

async function chargerMesSeries() {
  const conteneur = document.getElementById("liste-series");
  conteneur.innerHTML = "<p>Chargement...</p>";

  let series;
  try {
    series = await appelApi(`${API}/utilisateurs/${getUtilisateurId()}/series`);
  } catch (err) {
    conteneur.innerHTML = `<p>Erreur : ${err.message}</p>`;
    return;
  }

  if (series.length === 0) {
    conteneur.innerHTML = messageVide("Aucune série dans votre liste pour l'instant. Ajoutez-en une ci-dessus ou explorez l'onglet Découvrir.");
    seriesEnMemoire = [];
    return;
  }

  seriesEnMemoire = [];
  for (const serie of series) {
    let progression = null;
    try {
      progression = await appelApi(`${API}/utilisateurs/${getUtilisateurId()}/progression/${serie.id}`);
    } catch {
      // No watch data yet for this series — not an error, progress bar just won't show
    }
    seriesEnMemoire.push({ serie, progression });
  }

  rendreMesSeries();
}

function trierEtFiltrerSeries() {
  const tri = document.getElementById("select-tri").value;
  const filtreGenre = document.getElementById("input-filtre-genre").value.trim().toLowerCase();

  let liste = [...seriesEnMemoire];

  if (filtreGenre) {
    liste = liste.filter((item) => (item.serie.genre || "").toLowerCase().includes(filtreGenre));
  }

  const comparateurs = {
    "titre-asc": (a, b) => a.serie.titre.localeCompare(b.serie.titre),
    "titre-desc": (a, b) => b.serie.titre.localeCompare(a.serie.titre),
    "annee-desc": (a, b) => (b.serie.anneeSortie || 0) - (a.serie.anneeSortie || 0),
    "annee-asc": (a, b) => (a.serie.anneeSortie || 0) - (b.serie.anneeSortie || 0),
    "note-desc": (a, b) => (b.serie.note || 0) - (a.serie.note || 0),
    "progression-desc": (a, b) => (b.progression?.pourcentage || 0) - (a.progression?.pourcentage || 0),
  };
  liste.sort(comparateurs[tri] || comparateurs["titre-asc"]);

  return liste;
}

function rendreMesSeries() {
  const conteneur = document.getElementById("liste-series");
  const liste = trierEtFiltrerSeries();

  if (liste.length === 0) {
    conteneur.innerHTML = messageVide("Aucune série ne correspond à ce filtre.");
    return;
  }

  conteneur.innerHTML = "";
  for (const { serie, progression } of liste) {
    const carte = creerElement(`
      <div class="serie-card" data-id="${serie.id}">
        <img src="${serie.imageUrl || IMAGE_PLACEHOLDER}" alt="Affiche de ${serie.titre}"
             onerror="this.src='${IMAGE_PLACEHOLDER}'">
        <div class="contenu">
          <h3>${serie.titre}</h3>
          <span class="meta">${serie.genre || ''} ${serie.anneeSortie ? '· ' + serie.anneeSortie : ''}</span>
          ${progression ? `
            <div class="barre-progression">
              <div class="remplissage" style="width:${progression.pourcentage}%"></div>
            </div>
            <span class="meta">${progression.episodesVus}/${progression.episodesTotal} épisodes</span>
          ` : ''}
          <button class="btn-supprimer" data-id="${serie.id}">Supprimer</button>
        </div>
      </div>
    `);

    carte.querySelector(".btn-supprimer").addEventListener("click", async (e) => {
      e.stopPropagation();
      if (!confirm(`Supprimer "${serie.titre}" ?`)) return;
      await appelApi(`${API}/utilisateurs/${getUtilisateurId()}/series/${serie.id}`, { method: "DELETE" });
      chargerMesSeries();
    });

    carte.addEventListener("click", () => ouvrirDetailSerie(serie));
    conteneur.appendChild(carte);
  }
}

document.getElementById("select-tri").addEventListener("change", rendreMesSeries);
document.getElementById("input-filtre-genre").addEventListener("input", rendreMesSeries);

document.getElementById("form-ajout-serie").addEventListener("submit", async (e) => {
  e.preventDefault();
  const titre = document.getElementById("input-titre").value;
  const genre = document.getElementById("input-genre").value;
  await appelApi(`${API}/utilisateurs/${getUtilisateurId()}/series`, {
    method: "POST",
    body: JSON.stringify({ titre, genre }),
  });
  e.target.reset();
  chargerMesSeries();
});

// --- Detail panel (accessible modal) ---

let elementDeclencheur = null;

function ouvrirModale() {
  const panneau = document.getElementById("panneau-detail");
  elementDeclencheur = document.activeElement;
  panneau.classList.remove("hidden");
  panneau.setAttribute("aria-hidden", "false");
  document.body.style.overflow = "hidden";
  const premierFocusable = panneau.querySelector("button, [href], input, select, textarea, [tabindex]");
  premierFocusable?.focus();
  document.addEventListener("keydown", gererClavierModale);
}

function fermerModale() {
  const panneau = document.getElementById("panneau-detail");
  panneau.classList.add("hidden");
  panneau.setAttribute("aria-hidden", "true");
  document.body.style.overflow = "";
  document.removeEventListener("keydown", gererClavierModale);
  elementDeclencheur?.focus();
}

function gererClavierModale(e) {
  const panneau = document.getElementById("panneau-detail");
  if (e.key === "Escape") { fermerModale(); return; }
  if (e.key === "Tab") {
    const focusables = panneau.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
    const premier = focusables[0];
    const dernier = focusables[focusables.length - 1];
    if (e.shiftKey && document.activeElement === premier) { e.preventDefault(); dernier.focus(); }
    else if (!e.shiftKey && document.activeElement === dernier) { e.preventDefault(); premier.focus(); }
  }
}

document.getElementById("panneau-detail").addEventListener("click", (e) => {
  if (e.target.id === "panneau-detail") fermerModale();
});

async function ouvrirDetailSerie(serie) {
  const contenu = document.getElementById("detail-contenu");
  contenu.innerHTML = `<h2 id="titre-detail">${serie.titre}</h2><p>Chargement...</p>`;
  ouvrirModale();

  const saisons = await appelApi(`${API}/utilisateurs/${getUtilisateurId()}/series/${serie.id}/saisons`);

  if (saisons.length === 0) {
    contenu.innerHTML = `<h2 id="titre-detail">${serie.titre}</h2>` + messageVide("Aucune saison enregistrée pour cette série.");
    return;
  }

  let html = `<h2 id="titre-detail">${serie.titre}</h2>`;
  for (const saison of saisons) {
    const episodes = await appelApi(`${API}/saisons/${saison.id}/episodes`);
    html += `<h3>Saison ${saison.numero}</h3>`;
    if (episodes.length === 0) {
      html += messageVide("Aucun épisode enregistré pour cette saison.");
      continue;
    }
    for (const ep of episodes) {
      html += `
        <div class="episode-ligne" data-episode-id="${ep.id}">
          <span>Épisode ${ep.numero} — ${ep.titre || ''}</span>
          <input type="checkbox" class="check-vu" data-episode-id="${ep.id}" id="ep-${ep.id}">
          <label for="ep-${ep.id}" class="sr-only">Marquer l'épisode ${ep.numero} comme vu</label>
        </div>
      `;
    }
  }
  contenu.innerHTML = html;

  contenu.querySelectorAll(".check-vu").forEach((checkbox) => {
    checkbox.addEventListener("change", async (e) => {
      const episodeId = e.target.dataset.episodeId;
      const ligne = e.target.closest(".episode-ligne");
      try {
        if (e.target.checked) {
          await appelApi(`${API}/utilisateurs/${getUtilisateurId()}/visionnages/${episodeId}`, { method: "POST" });
          ligne.classList.add("vu");
        } else {
          await appelApi(`${API}/utilisateurs/${getUtilisateurId()}/visionnages/${episodeId}`, { method: "DELETE" });
          ligne.classList.remove("vu");
        }
      } catch (err) {
        alert(err.message);
        e.target.checked = !e.target.checked;
      }
    });
  });
}

document.getElementById("btn-fermer-panneau").addEventListener("click", () => {
  fermerModale();
  chargerMesSeries();
});

// --- TMDB discovery ---

function afficherResultatsTmdb(series, conteneurId = "resultats-tmdb") {
  const conteneur = document.getElementById(conteneurId);

  if (!series || series.length === 0) {
    conteneur.innerHTML = messageVide("Aucun résultat pour cette recherche.");
    return;
  }

  conteneur.innerHTML = "";
  for (const serie of series) {
    const carte = creerElement(`
      <div class="serie-card">
        <img src="${serie.imageUrl || IMAGE_PLACEHOLDER}" alt="Affiche de ${serie.titre}"
             onerror="this.src='${IMAGE_PLACEHOLDER}'">
        <div class="contenu">
          <h3>${serie.titre}</h3>
          <span class="meta">${serie.dateDiffusion || 'Date inconnue'} · ⭐ ${serie.note?.toFixed(1) || '?'}</span>
          <button class="btn-importer" data-tmdb-id="${serie.tmdbId}">Importer</button>
        </div>
      </div>
    `);

    carte.querySelector(".btn-importer").addEventListener("click", async (e) => {
      e.stopPropagation();
      const btn = e.target;
      btn.disabled = true;
      btn.textContent = "Import...";
      try {
        await appelApi(`${API}/utilisateurs/${getUtilisateurId()}/tmdb/importer/${serie.tmdbId}`, { method: "POST" });
        btn.textContent = "Importé ✓";
      } catch (err) {
        alert(err.message);
        btn.disabled = false;
        btn.textContent = "Importer";
      }
    });

    conteneur.appendChild(carte);
  }
}

// Sub-category buttons: Populaires / Tendances / Mieux notées / Bientôt
document.querySelectorAll(".souscat-btn").forEach((btn) => {
  btn.addEventListener("click", async () => {
    document.querySelectorAll(".souscat-btn").forEach((b) => b.classList.remove("active"));
    btn.classList.add("active");

    const routes = {
      populaires: `${API}/tmdb/populaires`,
      tendances: `${API}/tmdb/tendances`,
      "mieux-notees": `${API}/tmdb/mieux-notees`,
      "diffusees-bientot": `${API}/tmdb/diffusees-bientot`,
    };

    document.getElementById("resultats-tmdb").innerHTML = "<p>Chargement...</p>";
    try {
      const resultats = await appelApi(routes[btn.dataset.cat]);
      afficherResultatsTmdb(resultats);
    } catch (err) {
      document.getElementById("resultats-tmdb").innerHTML = `<p>Erreur : ${err.message}</p>`;
    }
  });
});

document.getElementById("form-recherche-tmdb").addEventListener("submit", async (e) => {
  e.preventDefault();
  const titre = document.getElementById("input-recherche").value;
  if (!titre) return;
  try {
    const resultats = await appelApi(`${API}/tmdb/recherche?titre=${encodeURIComponent(titre)}`);
    afficherResultatsTmdb(resultats);
  } catch (err) {
    document.getElementById("resultats-tmdb").innerHTML = `<p>Erreur : ${err.message}</p>`;
  }
});

// --- Discover: genre / year / rating filter ---

async function chargerGenres() {
  try {
    const genres = await appelApi(`${API}/tmdb/genres`);
    const select = document.getElementById("select-genre");
    for (const genre of genres) {
      const option = document.createElement("option");
      option.value = genre.id;
      option.textContent = genre.nom;
      select.appendChild(option);
    }
  } catch {
    // Genre dropdown just stays with "Tous les genres" if TMDB is unreachable
  }
}

document.getElementById("form-decouvrir").addEventListener("submit", async (e) => {
  e.preventDefault();
  const genre = document.getElementById("select-genre").value;
  const annee = document.getElementById("input-annee").value;
  const noteMin = document.getElementById("input-note-min").value;

  const params = new URLSearchParams();
  if (genre) params.set("genre", genre);
  if (annee) params.set("annee", annee);
  if (noteMin) params.set("noteMin", noteMin);

  document.getElementById("resultats-tmdb").innerHTML = "<p>Chargement...</p>";
  try {
    const resultats = await appelApi(`${API}/tmdb/decouvrir?${params.toString()}`);
    afficherResultatsTmdb(resultats);
  } catch (err) {
    document.getElementById("resultats-tmdb").innerHTML = `<p>Erreur : ${err.message}</p>`;
  }
});

// --- Actor search + filmography ---

document.getElementById("form-recherche-acteur").addEventListener("submit", async (e) => {
  e.preventDefault();
  const nom = document.getElementById("input-acteur").value;
  if (!nom) return;

  const conteneur = document.getElementById("resultats-acteurs");
  conteneur.innerHTML = "<p>Chargement...</p>";

  let acteurs;
  try {
    acteurs = await appelApi(`${API}/tmdb/recherche-acteur?nom=${encodeURIComponent(nom)}`);
  } catch (err) {
    conteneur.innerHTML = `<p>Erreur : ${err.message}</p>`;
    return;
  }

  if (acteurs.length === 0) {
    conteneur.innerHTML = messageVide("Aucun acteur trouvé pour cette recherche.");
    return;
  }

  conteneur.innerHTML = "";
  for (const acteur of acteurs) {
    const carte = creerElement(`
      <div class="serie-card acteur-card" data-acteur-id="${acteur.id}">
        <img src="${acteur.photoUrl || IMAGE_PLACEHOLDER}" alt="Photo de ${acteur.nom}"
             onerror="this.src='${IMAGE_PLACEHOLDER}'">
        <div class="contenu"><h3>${acteur.nom}</h3></div>
      </div>
    `);
    carte.addEventListener("click", () => afficherFilmographie(acteur));
    conteneur.appendChild(carte);
  }
});

async function afficherFilmographie(acteur) {
  const conteneur = document.getElementById("resultats-acteurs");
  conteneur.innerHTML = `<p>Chargement de la filmographie de ${acteur.nom}...</p>`;

  let series;
  try {
    series = await appelApi(`${API}/tmdb/acteur/${acteur.id}/series`);
  } catch (err) {
    conteneur.innerHTML = `<p>Erreur : ${err.message}</p>`;
    return;
  }

  conteneur.innerHTML = `<h3 class="card">Séries avec ${acteur.nom}</h3>`;
  const grille = creerElement('<div class="grid"></div>');
  conteneur.appendChild(grille);
  // afficherResultatsTmdb(series, null); // placeholder call replaced below
  // afficherResultatsTmdb expects an element id, so render directly into the fresh grid instead:
  grille.id = "resultats-filmographie";
  afficherResultatsTmdb(series, "resultats-filmographie");
}

// --- Startup ---

if (getToken()) {
  afficherApp(getPseudo());
} else {
  afficherLogin();
}