# Contribuire

Grazie per l'interesse. Per modifiche non banali, apri prima una issue per discuterne: evita lavoro sprecato.

## Licenza dei contributi

Contribuendo accetti che il tuo contributo sia rilasciato sotto la stessa licenza del progetto ([PolyForm Noncommercial 1.0.0](../LICENSE)): uso non commerciale, con attribuzione.

## Build

Vedi il [README](../README.md). In breve: JDK 17+, Android SDK, dalla cartella `android/` esegui `./gradlew assembleDebug`.

## Stile del codice

- Kotlin idiomatico, coerente col codice esistente. Per la UI Compose segui i pattern già presenti e [docs/design-principles.md](../docs/design-principles.md).
- Commenti: spiega il **perché**, non il **cosa**. Il codice deve essere leggibile da solo; niente commenti che ripetono ciò che il codice già dice.
- Niente slop, per favore.

## Commit e pull request

- Un commit = una modifica coerente. Messaggi in imperativo e descrittivi (cosa e perché), non "fix", "update", "wip".
- Non riscrivere la storia di `main` (niente force-push). Le PR partono da un branch.
- La CI (build) deve passare prima del merge.
- Fine-riga LF (vedi `.gitattributes`); in particolare `gradlew` deve restare LF.