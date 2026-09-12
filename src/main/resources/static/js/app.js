// js/app.js
// Vanilla JS, no build step — fetch() against the same-origin Spring Boot API.

const API = "/api";

// --- Auth / session storage ---

function getToken() {
  return localStorage.getItem("token");
}

function getUtilisateurId() {
  return localStorage.getItem("utilisateurId");
}

function getPseudo() {
  return localStorage.getItem("pseudo");
}

// Single place that writes everything the session needs after login/register
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

// --- Auth tabs (login / inscription) ---

document.querySelectorAll(".auth-tab").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".auth-tab").forEach((b) => b.classList.remove("active"));
    document.querySelectorAll(".auth-panel").forEach((p) => p.classList.remove("active"));
    btn.classList.add("active");
    document.getElementById(`form-${btn.dataset.auth}`).classList.add("active");
    document.getElementById("login-erreur").textContent = "";
  });
});

// --- Generic API call wrapper ---

async function appelApi(url, options = {}) {
  const token = getToken();
  const headers = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const reponse = await fetch(url, { headers, ...options });

  if (reponse.status === 401 || reponse.status === 403) {
    // Missing/expired token → force back to login, unless this WAS the login/register call
    if (!url.includes("/auth/")) {
      deconnexion();
    }
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

let elementDeclencheur = null; // remembers what had focus before the modal opened, to restore it on close

function ouvrirModale() {
  const panneau = document.getElementById("panneau-detail");
  elementDeclencheur = document.activeElement;

  panneau.classList.remove("hidden");
  panneau.setAttribute("aria-hidden", "false");
  document.body.style.overflow = "hidden"; // block background scroll

  // Focus the first focusable element inside the modal
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
  elementDeclencheur?.focus(); // return focus to whatever opened the modal
}

function gererClavierModale(e) {
  const panneau = document.getElementById("panneau-detail");

  if (e.key === "Escape") {
    fermerModale();
    return;
  }

  if (e.key === "Tab") {
    // Focus trap: keep Tab/Shift+Tab cycling within the modal only
    const focusables = panneau.querySelectorAll(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    const premier = focusables[0];
    const dernier = focusables[focusables.length - 1];

    if (e.shiftKey && document.activeElement === premier) {
      e.preventDefault();
      dernier.focus();
    } else if (!e.shiftKey && document.activeElement === dernier) {
      e.preventDefault();
      premier.focus();
    }
  }
}

// Click on the overlay (outside .panneau-contenu) closes the modal
document.getElementById("panneau-detail").addEventListener("click", (e) => {
  if (e.target.id === "panneau-detail") fermerModale();
});

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

// --- Tabs ---

document.querySelectorAll(".tab-btn").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tab-btn").forEach((b) => b.classList.remove("active"));
    document.querySelectorAll(".tab-panel").forEach((p) => p.classList.remove("active"));
    btn.classList.add("active");
    document.getElementById(btn.dataset.tab).classList.add("active");
  });
});

// --- My series ---

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
  conteneur.innerHTML = "";

  for (const serie of series) {
    let progression = null;
    try {
      progression = await appelApi(`${API}/utilisateurs/${getUtilisateurId()}/progression/${serie.id}`);
    } catch {
      // No watch data yet for this series — not an error, just skip the progress bar
    }

    const carte = creerElement(`
      <div class="serie-card" data-id="${serie.id}">
        <img src="${serie.imageUrl || ''}" alt="${serie.titre}" onerror="this.style.display='none'">
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

// --- Detail serie: seasons, episodes, progress ---

async function ouvrirDetailSerie(serie) {
  const panneau = document.getElementById("panneau-detail");
  const contenu = document.getElementById("detail-contenu");
  contenu.innerHTML = `<h2>${serie.titre}</h2><p>Chargement...</p>`;
  panneau.classList.remove("hidden");

  const saisons = await appelApi(`${API}/utilisateurs/${getUtilisateurId()}/series/${serie.id}/saisons`);

  let html = `<h2>${serie.titre}</h2>`;
  for (const saison of saisons) {
    const episodes = await appelApi(`${API}/saisons/${saison.id}/episodes`);
    html += `<h3>Saison ${saison.numero}</h3>`;
    for (const ep of episodes) {
      html += `
        <div class="episode-ligne" data-episode-id="${ep.id}">
          <span>Épisode ${ep.numero} — ${ep.titre || ''}</span>
          <input type="checkbox" class="check-vu" data-episode-id="${ep.id}">
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
  document.getElementById("panneau-detail").classList.add("hidden");
  chargerMesSeries();
});

// --- TMDB discovery ---

function afficherResultatsTmdb(series) {
  const conteneur = document.getElementById("resultats-tmdb");
  conteneur.innerHTML = "";

  for (const serie of series) {
    const carte = creerElement(`
      <div class="serie-card">
        <img src="${serie.imageUrl || ''}" alt="${serie.titre}" onerror="this.style.display='none'">
        <div class="contenu">
          <h3>${serie.titre}</h3>
          <span class="meta">${serie.dateDiffusion || ''} · ⭐ ${serie.note?.toFixed(1) || '?'}</span>
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

document.getElementById("form-recherche-tmdb").addEventListener("submit", async (e) => {
  e.preventDefault();
  const titre = document.getElementById("input-recherche").value;
  if (!titre) return;
  const resultats = await appelApi(`${API}/tmdb/recherche?titre=${encodeURIComponent(titre)}`);
  afficherResultatsTmdb(resultats);
});

document.getElementById("btn-populaires").addEventListener("click", async () => {
  const resultats = await appelApi(`${API}/tmdb/populaires`);
  afficherResultatsTmdb(resultats);
});

// --- Startup ---

if (getToken()) {
  afficherApp(getPseudo());
} else {
  afficherLogin();
}