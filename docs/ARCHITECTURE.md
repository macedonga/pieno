# Pieno — architettura e funzionamento

App Android nativa che replica e migliora l'esperienza dell'app ufficiale della
tessera carburante agevolata del Friuli Venezia Giulia. Indipendente, non
affiliata a Insiel né alla Regione FVG. Uso personale e di interoperabilità.

Design e regole UI: [design-principles.md](design-principles.md).

## Idea di fondo

Il QR della tessera è un **token firmato dal backend regionale** (formato EUDCC /
Green Pass). L'app lo **scarica e lo ridisegna** così com'è: non lo modifica e non
fa crittografia lato client. Modificarlo romperebbe la firma e la pompa lo
rifiuterebbe. Tutto il resto (mappa, ricerca, storico) è costruito attorno a questo.

## Funzionalità

- **Tessera**: QR in evidenza, disponibile anche offline (il payload firmato è
  salvato in locale e mostrato anche senza rete); tocca il QR per ingrandirlo a
  schermo intero con luminosità al massimo. Carosello multi-tessera (proprie e
  condivise) con nome e colore personalizzabili e riordino animato.
- **Trova il più economico**: per l'auto della tessera, cerca il prezzo più basso
  entro un raggio che scegli (e che l'app ricorda); a parità di prezzo, il più vicino.
- **Stazioni**: lista e mappa (OpenStreetMap, senza API key) dei distributori
  convenzionati, con tutti i prezzi, ordinati per vicinanza, preferiti in cima,
  filtro carburante sulla mappa; dettaglio con i tuoi acquisti lì e navigazione.
- **Storico** rifornimenti per mese, con speso e sconto risparmiato.
- **Tessere condivise**: importa il QR di un'auto non tua (scansione o incolla),
  con verifica della firma in locale.
- **Google Wallet**: pass col QR, personalizzato e condivisibile (vedi sotto).
- **Profilo** con anagrafica mascherata, comunicazioni, tema chiaro/scuro/sistema.
- **Demo**: senza login, dati di esempio per provare l'interfaccia.

## Stack

- Kotlin, Jetpack Compose (Material3 ritematizzato). AGP 9.2.x, Gradle 9.4.x,
  compileSdk 36, minSdk 26.
- Stato in Compose + `Repository` come sorgente unica dei dati; **DI manuale** via
  `PienoContainer` (niente Hilt). Navigation Compose con bottom nav e rotte annidate.
- Rete: Retrofit + OkHttp + kotlinx.serialization. Un interceptor inietta gli
  header di auth e fa refresh automatico del token su 401.
- Auth: AppAuth (OAuth2 Authorization Code + PKCE, Custom Tabs), LoginFVG (SPID/CIE).
  Redirect `it.insiel.benzapp.cittadino:/oauth2redirect`.
- Persistenza: DataStore (Preferences). I token sono cifrati con una chiave in
  Android Keystore, mai in chiaro né nei log.
- QR: ZXing (generazione). Scansione condivise: CameraX + ML Kit. Decodifica/verifica
  HC1: CBOR + java.security.
- Mappa: osmdroid con tile CartoDB (OpenStreetMap), senza API key.
- Google Wallet: play-services-pay (vedi sotto — la firma è lato server).

## Package (`dev.ceccon.pieno`)

- `ui/theme`, `ui/icons` — design system (colori, tipografia, dimensioni, icone custom).
- `ui/components` — bottoni, card, liste, top/bottom bar, stati, sheet, mappa.
- `ui/screens` — login, tessera (home), stazioni (+dettaglio), storico,
  comunicazioni, profilo, condivise, scan, riordino tessere.
- `data/model` — modelli di dominio.
- `data/api` — interfaccia Retrofit, DTO, interceptor.
- `data/auth` — AppAuth, token store cifrato.
- `data/local` — DataStore: token, cache QR offline, condivise, preferiti, prefs.
- `data/repo` — `Repository` (cache in memoria, invalidata al resume).
- `data/qr` — decodifica/verifica HC1 (Base45, zlib, COSE/CWT, ES256).
- `data/wallet` — chiede il JWT del pass al Worker e lo passa a Google Pay.
- `core`, `demo` — util/formattatori e dati di esempio per la modalità non loggata.

## API (sola lettura)

Base prod `https://api.regione.fvg.it/fuel-api`, filtri `q=campo:valore`.

- `GET beneficiari?q=codiceFiscale:<CF>` — il CF è il `sub` del JWT prima della `@`.
- `GET domande?q=idBeneficiario:<id>` — ogni `payload` è la stringa QR firmata.
- `GET rifornimenti?q=idDomanda:<id>` — importi come stringa decimale.
- `GET punti-vendita` e prezzi — per le stazioni NON inviare `X-USER-AUTHORIZATION`.
- `GET comunicazioni?q=soloCorrenti:<bool>`, `GET listaDiValori`.

Gotcha:
- Carburante in `Prezzo.idTipoCarburante`: 2 VERDE, 3 GASOLIO, 4 GPL, 5 METANO,
  6 L-CNG, 7 GNL (NON `tipoProdotto`, che è la fascia). Alias BENZINA→VERDE,
  DIESEL→GASOLIO.
- Il prezzo `1.000` è un sentinella "non impostato": va escluso dai minimi (ma NON
  i prezzi bassi reali tipo GPL ~0.7).
- Gli `id` reali sono UUID stringa, non numeri.
- L'anagrafica `beneficiari` spesso non porta email/telefono: si prendono dagli
  attributi SPID nel token.

## QR / HC1

`HC1:` + Base45 + zlib(deflate) + COSE_Sign1 (CBOR/CWT), firma ES256 (P-256).
Claim CWT: 1 paese, **4 = exp**, **6 = iat**, -260 contenitore HCERT `{1:{i:id,p:targa}}`.

Nota importante: la **scadenza (claim 4) è un sentinella far-future** (anno ~4762,
in millisecondi): il QR **non ruota e di fatto non scade**. Per questo le tessere
condivise (importate una volta) restano valide. Mostrare il QR = ridisegnare la
stringa. Per le condivise si decodifica per estrarre targa/firma e si **verifica la
firma** contro il certificato pubblico regionale (CN "Sistema Carburanti Agevolati
FVG"); se non è valida, l'import è rifiutato. Tutto in locale.

## Google Wallet (firma lato server)

Il pulsante "Aggiungi a Google Wallet" è attivo di default. La chiave del service
account **non sta nell'app** (sarebbe estraibile dall'APK): un piccolo **Cloudflare
Worker** (te lo crei te) firma il JWT e fa l'upsert
dell'oggetto via Wallet API, così colore/nome/icona/bottone si aggiornano anche sui
pass già salvati. L'app fa POST a `BuildConfig.WALLET_SIGNER_URL` (default impostato,
sovrascrivibile con `-PwalletSignerUrl=...`) e passa il JWT a Google Pay. Il pass è
personalizzabile (nome ed etichetta della tessera, colore), condivisibile, e ha un
pulsante che riapre l'app sullo Storico (deep link `pieno://storico`).

## Demo

Senza login l'app mostra dati di esempio realistici in sola lettura, così
l'interfaccia è completa e provabile sull'emulatore senza SPID.

## Sicurezza e privacy

- Token cifrati (Keystore), mai in chiaro né in log; nessun proxy, niente dati a terzi.
- Dati sensibili (email, telefono, CF) mascherati di default in UI.
- Nessuna scrittura sull'account: l'app legge e mostra. Niente sconti non spettanti.

## Login reale

Il login (LoginFVG, SPID/CIE) va fatto sul telefono: l'emulatore non supporta SPID
(da cui la modalità demo). Il redirect OAuth usa lo schema
`it.insiel.benzapp.cittadino`, lo stesso dell'app ufficiale: se hai l'app ufficiale
installata, Android potrebbe chiedere con quale app aprire il redirect — scegli
Pieno (o disinstalla l'ufficiale). Dopo il primo accesso il token si rinnova da solo.

## Tessere condivise

Profilo → Tessere condivise → Scansiona (fotocamera) o Incolla la stringa `HC1:...`.
La firma è verificata in locale contro il certificato pubblico regionale; se non è
valida, l'import è rifiutato. Tutto in locale, senza server.

## Posizione

"Distributori vicini" e "trova il più economico" usano la posizione (permesso al
primo uso). Senza permesso o GPS, la lista resta ordinata e la mappa parte
sull'intero Friuli Venezia Giulia.

## Font

Display Fraunces, testo Hanken Grotesk (in `app/src/main/res/font`), licenza SIL
Open Font License (OFL).

## Limitazioni

Login reale e API sono verificabili solo su un device con account SPID/CIE. L'app è
in sola lettura: non attiva tessere né registra dispositivi sull'account.
