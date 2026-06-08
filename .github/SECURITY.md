# Security Policy

Pieno gestisce dati sensibili (token OAuth/SPID, QR della tessera). Le segnalazioni di sicurezza responsabili sono benvenute.

## Segnalare una vulnerabilità

**Non** aprire una issue pubblica per un problema di sicurezza. Usa il canale privato:

- scheda **Security** del repo → **Report a vulnerability** (private vulnerability reporting). In alternativa, contatta il maintainer su GitHub.

Includi: descrizione, passi per riprodurre, impatto e, se puoi, una proposta di fix. Si cerca di rispondere entro pochi giorni.

## Ambito

- L'app accede **solo ai dati dell'account dell'utente**, tramite le API ufficiali e il login SPID/CIE dell'utente; non manda nulla a terzi. I token sono cifrati con Android Keystore.
- Aree particolarmente rilevanti: gestione e refresh dei token, storage cifrato, verifica della firma dei QR condivisi, e il signer Google Wallet (lato server).

## Versioni supportate

Si lavora sull'ultima build (`main` / release `nightly`): riferisci le segnalazioni a quella.
