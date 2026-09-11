// js/app.js
// Vanilla JS, no build step — fetch() against the same-origin Spring Boot API.

const API = "/api";
const UTILISATEUR_ID = 1; // simplifié pour l'examen : un seul utilisateur de démo

// --- Utilitaires ---

async function appelApi(url, options = {}) {
  const reponse = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
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

// --- Onglets ---

document.querySelectorAll(".tab-btn").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tab-btn").forEach((b) => b.classList.remove("active"));
    document.querySelectorAll(".tab-panel").forEach((p) => p.classList.remove("active"));
    btn.classList.add("active");
    document.getElementById(btn.dataset.tab).classList.add("active");
  });
});

// --- Mes séries ---

async function chargerMesSeries() {
  const conteneur = document.getElementById("liste-series");
  conteneur.innerHTML = "<p>Chargement...</p>";

  const series = await appelApi(`${API}/series`);
  conteneur.innerHTML = "";

  for (const serie of series) {
    let progression = null;
    try {
      progression = await appelApi(`${API}/utilisateurs/${UTILISATEUR_ID}/progression/${serie.id}`);
    } catch {
      // pas d'épisodes enregistrés pour cette série — pas grave, on affiche sans barre
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
      await appelApi(`${API}/series/${serie.id}`, { method: "DELETE" });
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

  await appelApi(`${API}/series`, {
    method: "POST",
    body: JSON.stringify({ titre, genre }),
  });

  e.target.reset();
  chargerMesSeries();
});

// --- Détail série : saisons, épisodes, progression ---

async function ouvrirDetailSerie(serie) {
  const panneau = document.getElementById("panneau-detail");
  const contenu = document.getElementById("detail-contenu");
  contenu.innerHTML = `<h2>${serie.titre}</h2><p>Chargement...</p>`;
  panneau.classList.remove("hidden");

  const saisons = await appelApi(`${API}/series/${serie.id}/saisons`);

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
          await appelApi(`${API}/utilisateurs/${UTILISATEUR_ID}/visionnages/${episodeId}`, { method: "POST" });
          ligne.classList.add("vu");
        } else {
          await appelApi(`${API}/utilisateurs/${UTILISATEUR_ID}/visionnages/${episodeId}`, { method: "DELETE" });
          ligne.classList.remove("vu");
        }
      } catch (err) {
        alert(err.message);
        e.target.checked = !e.target.checked; // annule visuellement si le serveur refuse
      }
    });
  });
}

document.getElementById("btn-fermer-panneau").addEventListener("click", () => {
  document.getElementById("panneau-detail").classList.add("hidden");
  chargerMesSeries(); // rafraîchit les barres de progression après fermeture
});

// --- Découvrir via TMDB ---

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
        await appelApi(`${API}/tmdb/importer/${serie.tmdbId}`, { method: "POST" });
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

// --- Démarrage ---

chargerMesSeries();