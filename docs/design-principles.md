# Pieno, principi di design e regole

Questo documento e' la fonte di verita' per le decisioni di design dell'app.
Va letto e applicato prima di scrivere UI. Non si prende una decisione estetica
"a istinto": la si motiva.

Pieno e' un'app indipendente. Non e' affiliata, sponsorizzata o approvata da
Insiel S.p.A. ne' dalla Regione Autonoma Friuli Venezia Giulia. Il disclaimer
deve essere visibile nell'app (schermata Profilo / Info).

## 0. Metodo: ragiona prima di ogni decisione

Per ogni scelta di design non banale, prima di implementarla, esplicita:

1. Obiettivo: cosa deve ottenere l'utente o l'interfaccia in quel punto.
2. Principio: quale regola o euristica si applica (gerarchia, contrasto, ritmo,
   allineamento, bordi concentrici, prossimita', coerenza dei token).
3. Alternative: almeno due opzioni reali, non una sola.
4. Scelta e perche': quale opzione e perche' batte le altre rispetto all'obiettivo.

Le decisioni rilevanti si registrano nel "Registro decisioni" in fondo (DL-xxx).
Se una scelta non sa rispondere a "perche' non l'altra opzione", non e' pronta.

## 1. Regole apprese dalle correzioni

Queste vengono da feedback diretto e hanno priorita'.

### R1. Bordi concentrici
Quando un elemento arrotondato sta dentro un altro con padding `p`, il raggio
interno deve essere `raggio_esterno - p`, cosi' gli angoli sono paralleli
(concentrici). Errore commesso e da non ripetere: card raggio 28 con padding 22
e pannello interno raggio 20 (avrebbe dovuto essere 6 per essere concentrico).
Si sceglie quindi prima il raggio esterno e il padding, poi il raggio interno
ne deriva. Non si mettono raggi a caso.

### R2. Niente elementi "buttati li'"
Ogni testo o controllo deve avere una collocazione deliberata: gerarchia
(etichetta piccola a bassa enfasi sopra il valore in evidenza), allineamento a
una griglia, e quando serve un contenitore o un filo divisore che gli dia un
posto. Errore commesso: "Valida fino al 17 giugno" come testo isolato sotto il
QR, senza struttura.

Aggiornamento: la card della tessera ora mostra solo intestazione e QR. La
validita' non si scrive piu' (si aggiornava da sola ed era rumore): il payload
e il token si rinnovano in automatico al ritorno sullo schermo. Stesso principio
applicato all'intestazione del menu Opzioni: la targa grigia "buttata" sotto il
titolo e' diventata una pillola verde a destra, coerente con quella sulla card.

## 2. Anti "AI slop"

Da evitare sempre:
- Trattini lunghi (em-dash). Usare virgole, due punti, parentesi, punti.
- Emoji nell'interfaccia.
- "Eyebrow" in monospace (piccole etichette maiuscole in carattere a larghezza
  fissa usate come decorazione).
- Badge e pillole-notifica gratuite. Per "non letto" usare un indicatore
  discreto (testo "Nuovo" sobrio o un punto piccolo), non un badge numerico.
- Glassmorphism e neumorphism decorativi. Ammessi solo se risolvono un problema
  reale di profondita', mai come effetto fine a se stesso. Default: superfici
  piatte, gerarchia per colore e spazio, non per ombre vistose.
- Gradienti arcobaleno, ombre forti, bordi multipli, accenti multipli.

Direzione voluta: sobria, editoriale, sicura. Lo spazio bianco e la tipografia
fanno il lavoro, non gli effetti.

## 3. Token

### Colore
Palette a tre primari piu' un accento, tutti ad alto contrasto.
- paper `#F6F4EF` (sfondo, carta calda, evita il bianco clinico)
- ink `#15161A` (testo primario, quasi nero caldo)
- inkSoft `#5B5D66` (testo secondario)
- hairline `#E5E1D8` (divisori, bordi sottili)
- surface `#FFFFFF` (card chiare)
- green `#0E4F3C` / greenDeep `#093528` (brand, superfici forti, tessera)
- accent `#E1502A` (vermiglio, CTA e numeri chiave, sconto)
Varianti soft e set scuro completo in `Color.kt`.

Contrasto: testo primario su sfondo deve superare 7:1; testo secondario almeno
4.5:1; testo su accent o verde almeno 4.5:1 per testo normale o usato solo su
testo grande/bold. Bianco su green `#0E4F3C` circa 9:1 (ok). Bianco su accent
`#E1502A` circa 3.9:1, quindi accent come sfondo solo per testo grande o bold.

### Raggi (regola R1)
Scala volutamente contenuta: arrotondamenti discreti, non morbidi (correzione
dell'utente: i raggi precedenti erano troppo arrotondati).
- r0 = 0, rXs = 6, rSm = 8, rMd = 12, rLg = 16, rXl = 20, rCard = 24, rPill = 999
- Coppia di riferimento: card `rCard` 24 con padding 16, pannello interno `rSm` 8
  (24 - 16 = 8, concentrico). I controlli (bottoni, campi) usano `rMd` 12. Le card
  generiche `rXl` 20. Le chip piccole `rSm` 8. Gli elementi a pillola usano `rPill`.
- Il segmentato usa esterno `rMd` 12 con padding 4, quindi pollice `rSm` 8
  (12 - 4 = 8, concentrico).

### Spaziatura
Griglia a 4dp. Scala: 4, 8, 12, 16, 20, 24, 32, 40, 48, 64.
Margine schermo standard: 20. Distanza tra blocchi: 24. Padding interno card: 16.

### Tipografia
Accoppiata display + testo per dare carattere senza perdere leggibilita', e per
evitare il look "tutto Inter" tipico delle UI generate.
- Display: Fraunces (serif morbido con optical sizing). Titoli grandi, nomi,
  numeri da mettere in scena. Pesi 580 to 650, opsz alto.
- Testo e UI: Hanken Grotesk. Corpo, etichette, bottoni, liste. Pesi 400/500/600.
- Numeri: dove si incolonnano (prezzi, litri, importi) si curano allineamento e
  spaziatura per leggerli come una tabella.
Entrambi i font sono inclusi nell'app (offline), licenza SIL OFL.

Scala (material3 Typography rimappata):
- displayLarge: Fraunces 40/44, tracking stretto
- headlineMedium: Fraunces 26/30
- titleLarge: Hanken 20/26 semibold
- titleMedium: Hanken 16/22 medium
- bodyLarge: Hanken 16/24
- bodyMedium: Hanken 14/20
- labelLarge: Hanken 14 semibold
- labelMedium: Hanken 12 medium

## 4. Layout e navigazione

- Griglia, ampio spazio bianco, margini coerenti.
- Bottom navigation a quattro voci, pollice friendly: Tessera, Stazioni, Storico,
  Profilo. La schermata di default e' Tessera perche' il compito primario e'
  mostrare il QR alla pompa, subito e anche offline.
- Minimizzare i passi per i compiti principali: il QR e' la home, non dietro un
  menu.
- Responsive: su schermi larghi i contenuti restano leggibili (max width dei
  contenuti, niente righe lunghissime).

## 5. Componenti e stati

Ogni schermata che carica dati prevede quattro stati: caricamento (skeleton
shimmer, non spinner nudo dove possibile), contenuto, vuoto (empty state con
spiegazione e azione), errore (messaggio chiaro e "Riprova"). Nessuno stato
lasciato bianco o ambiguo.

Feedback visivo per ogni azione: pressione bottoni (scala o cambio tono), esito
delle operazioni (conferma, errore), pull to refresh sulle liste.

## 6. Icone

Set custom disegnato a mano, coerente: griglia 24, tratto 1.75, estremi e
giunzioni arrotondati, stile lineare. Niente icon pack generico misto. Ogni
icona usata come pulsante ha una `contentDescription` (testo alternativo) in
italiano per l'accessibilita'.

## 7. Movimento

Microinterazioni sobrie e con uno scopo:
- transizioni di navigazione coerenti (fade e leggero slide)
- entrata della card QR (scala e fade brevi)
- indicatore di tab/pagina che scorre invece di apparire/sparire
- riordino tessere animato; pressione dei controlli
- shimmer durante i caricamenti
Durate brevi (150 to 300ms), curve naturali. Mai animazioni che rallentano l'uso.

## 8. Accessibilita'

- Aree toccabili almeno 48x48dp.
- Testo alternativo (contentDescription) su tutte le icone informative o
  interattive; icone puramente decorative marcate come tali.
- Contrasto AA o superiore (vedi sezione colore).
- Rispetto del tema chiaro/scuro di sistema.

## Registro decisioni

DL-001 Palette. Obiettivo: identita' distinta, professionale, ad alto contrasto,
a tema (carburante, agevolazione, FVG) e diversa dall'app ufficiale. Alternative:
verde/rosso da stazione (cliche'), blu fintech neutro (generico), verde
editoriale con accento vermiglio su carta calda. Scelta: verde profondo + carta
calda + vermiglio + inchiostro. Perche': il verde richiama agevolato/eco senza
essere letterale, il vermiglio da' energia e marca sconto e CTA, la carta calda
porta lo spazio bianco "editoriale" evitando il bianco clinico; tutto passa AA.

DL-002 Bordi concentrici. Vedi R1. Scelta: rCard 24, padding 16, pannello 8.
Perche': angoli paralleli leggibili come intenzionali; la combinazione 28/22/20
li violava. La scala e' stata poi ridotta (DL-008) restando concentrica.

DL-003 Tipografia. Obiettivo: gerarchia chiara con carattere, evitare il look
tutto-Inter. Alternative: singolo grotesque (pulito ma generico), serif display
+ sans pulito (editoriale). Scelta: Fraunces display + Hanken Grotesk testo.
Perche': il serif da' calore e artigianalita', il sans tiene la leggibilita';
entrambi OFL, inclusi per l'uso offline.

DL-004 Navigazione. Obiettivo: minimi passi al compito primario (QR alla pompa).
Scelta: bottom nav con Tessera come default. Perche': il QR e' visibile
all'avvio, anche offline; quattro destinazioni stanno in una mano.

DL-005 Footer validita' (superato). Prima scelta: footer con divisore + coppia
etichetta/valore + azione Aggiorna. Revisione dell'utente: la validita' e' inutile
da mostrare e il refresh manuale non serve. Scelta finale: footer rimosso, card =
intestazione + QR; aggiornamento di payload e token automatico al resume. Perche':
la home deve incantare per mostrare il QR all'esercente, senza dati di servizio.

DL-008 Raggi piu' contenuti. Obiettivo: l'utente trova tutta l'app troppo
arrotondata. Alternative: ritoccare solo le card grandi (incoerente con i
controlli), oppure ridurre l'intera scala mantenendo le proporzioni. Scelta:
ridurre tutta la scala (xl 28->20, card 32->24, md 16->12, ...) tenendo la regola
R1. Perche': coerenza, un solo gesto, angoli ancora ammorbiditi ma sobri.

DL-009 Indicatori che scorrono. Obiettivo: l'indicatore delle tab "scattava" da
uno stato all'altro. Alternative: incrociare dissolvenze (resta uno scatto
percepito), oppure un unico indicatore che trasla. Scelta: un solo indicatore
accent che scivola con una molla, sia nella bottom bar sia nei pallini del
carosello (questi seguono la posizione reale del pager durante lo swipe). Perche':
un'unica lingua di movimento, continua, coerente col pollice del segmentato.

DL-010 Mappa a tema. Obiettivo: la mappa stonava (tinte OSM accese, spillo
generico, partenza troppo larga, titolo netto sopra le tile). Scelta: filtro
colore sulle tile (smorzate in chiaro, invertite e desaturate in scuro), sfondo
carta; inquadratura iniziale e limite di scroll sul Friuli Venezia Giulia con
zoom minimo che non va piu' largo della regione; marker a pillola disegnato a
mano col prezzo e una punta; intestazione su gradiente carta->trasparente sopra
la mappa a tutto schermo. Perche': la mappa entra nel sistema visivo dell'app
invece di sembrare un widget incollato.

DL-011 Niente preferiti. Obiettivo: i preferiti duplicavano l'ordinamento
manuale. Scelta: rimossi del tutto, resta solo il riordino in "Gestisci tessere".
Perche': una sola leva chiara per decidere cosa vedere per primo, meno superficie.

DL-006 Stile icone. Scelta: lineari fatte a mano, griglia 24, tratto 1.75.
Perche': coerenza e identita' contro il misto da icon pack.

DL-007 Vincoli anti slop. Niente em-dash, emoji, eyebrow monospace, badge,
glass/neumorphism decorativi. Perche': la richiesta esplicita e' un design
curato e sobrio; questi elementi sono i marcatori tipici del "generato".
