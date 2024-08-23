[[_TOC_]]

# Wofür wird GIT verwendet?

In dieser Lehrveranstaltung werden Ihre Übungsausarbeitungen, sofern nicht explizit anders angeführt, über GitLab abgegeben bzw. eingereicht. Wichtig hierbei ist, dass wir den letzten gepushten Commit Ihres Masterbranches als maßgeblich für die Bewertung und die Bestimmung der von Ihnen gewählten Deadline ansehen. Die jeweiligen Punkte und Prüfungsergebnisse sind weiterhin in Moodle ersichtlich. 

**Ändern Sie nicht den Namen des Masterbranches**: Dieser muss den Namen `master` tragen. Nur Daten, die vor der Abgabedeadline im Masterbranch liegen (daher Commit samt Push *vor* der Deadline) werden während der Abgabegespräche und Bewertung berücksichtigt. Sie können eigene Branches anlegen, vergessen Sie dann aber nicht auf einen finalen Merge (unter Beibehaltung aller individuellen Commits & Commitmessages, daher *kein* squash) vor der Deadline in den `master` Branch.

# Wie erhalte ich lokalen Zugriff auf dieses Repository?

Um optimal mit diesem Repository zu arbeiten sollten Sie es auf Ihr lokales Arbeitsgerät spiegeln. Verwenden Sie hierzu den Befehl `git clone URLIhresRepositories`. Die URL Ihres Repositories finden Sie im Kopf dieser Webseite rechts vom Namen des Repositories. Um diese zu erhalten drücken Sie auf den blauen mit `Clone` beschrifteten Knopf. Wählen Sie die mittels `Clone with HTTPS` bereitgestellte URL. Diese sollte vergleichbar sein zu `https://git01lab.cs.univie.ac.at/.....`. 

**Probleme mit den Zertifikaten**: Falls Sie beim clonen Ihres Git Repositories Probleme gemeldet bekommen, die mit der Prüfung der Zertifikate in Verbindung stehen ist es eine schnelle Lösung diese abzuschalten. Hierzu kann folgender Befehl verwendet werden:  `git config --global http.sslVerify false`

# Wie nütze ich dieses Repository?

Clonen Sie hierzu dieses Repository wie oben angegeben. Danach können Sie mit `git add`, `commit`, `push`, etc. damit arbeiten. Optimalerweise legen Sie hierzu nach dem initialen clone Ihren Namen (echten Namen, kein Nickname) und Ihre E-Mail-Adresse (E-Mail-Adresse der Universität Wien) fest sodass alle Commits Ihnen direkt zugeordnet werden können. Verwenden Sie hierzu folgende Befehle:

> `git config --global user.name "Mein Name"`

> `git config --global user.email a123456@univie.ac.at`

**Hilfe und Unterstützung für Git/GitLab**: Weitere Hilfen samt einer schrittweisen Einführung in den Umgang mit Git finden sich im Git & GitLab Screencast auf Moodle. Dort ist auch direkt ein Skriptum eingebunden um Details nachzulesen. Für erfahrene Studierende, ist als Referenz, bei den Screencasts auch ein Git Cheat-Sheet verlinkt. An einem der Übungstermine findet auch ein Git Tutorial statt. Anschließend können Sie immer auch unseren Tutor mit Fragen zu Git/GitLab, z.B. [email](mailto:tutor.swe1@univie.ac.at) oder GitLab Issue kontaktieren.

Für weiterführende Informationen lohnt sich ein Blick in das Pro Git Handbuch: https://git-scm.com/book/de/v2 Besonders für das Thema branching empfiehlt sich außerdem: https://learngitbranching.js.org/ 

# Welche Inhalte sind vorgegeben und wofür sind diese gedacht?

Es wurden mehrere **Ordner** sowie **.gitignore** Dateien vorgegeben. Letztere dienen dazu Ihr Repository nicht mit "unnötigen" Dateien zu befüllen, welche es erschweren würden Ihr Projekt während der Abgabegespräche in die Entwicklungsumgebungen der Lektoren zu importieren (temporäre Dateien, etc.). Ändern Sie diese Dateien daher nicht bzw. nur sehr behutsam. 

Die vorgegebenen Ordner sind wie folgt zu verwenden:
* **Dokumentation** - Nützen Sie diesen Ordner, um Ihre _Dokumentation abzulegen_ bzw. abzugeben. Dies ist für *Teilaufgabe 1* relevant da Sie so Ihre Ausarbeitung (das zu erstellende PDF) hier hinterlegen können, um diese abzugeben. Eine Vorlage für die Ausarbeitung von Teilaufgabe 1 finden Sie in Moodle. Zusätzlich können Sie in diesem Ordner auch SVGs ablegen (für Klassen- und Sequenzdiagramme). Achten Sie darauf, dass die abgegebenen Inhalte lesbar sind. 

   Für *Teilaufgabe 2* und *3* können Sie hier die verlangte Quelle-Dokumentation als PDF ablegen um deutlich zu machen welche Ideen und Konzepte basierend auf welchen Quellen erstellt wurden. Bezüglich der Dateinamen und Pfade aller Abgaben die Angabe in Moodle beachten.
* **Executables** - Hinterlegen Sie hier die finalen _kompilierten Abgaben Ihrer Implementierung_ von Teilaufgabe 2 und Teilaufgabe 3. Diese sollten .jar Dateien seien, welche sich zumindest mit `java -jar <NameDerJarDatei.jar>` (plus die passenden Startargumente, siehe Moodle-Angabe) exekutieren lassen. Prüfen Sie ob dies der Fall ist! Genauere Informationen zu den zusätzlich anzuwendenden Parametern finden Sie auf Moodle und auf der Evaluierungsplattform. 

   Tipps dazu wie die notwendigen Jar Dateien erstellt werden können finden Sie ebenfalls Moodle (in der Angabe zum nachlesen oder auch _vorgezeigt_ im _Eclipse/IDE Screencast_ auf Moodle). Die Jar Dateien dienen nur zu Ihrer **Sicherheit** und sind nicht zwingend notwendig. Sollte sich während der Beurteilung Ihr Projekt nicht importieren oder bauen lassen wird auf die hier hinterlegten Jar Dateien zurückgegriffen (sollte dies notwendig werden kann dies zu Punkteabzügen führen). _Dieses Angebot stellt ein weiteres Sicherheitsnetz für Sie dar, wir empfehlen es zu nützen._
* **Source** - Nützen Sie diesen Ordner, um die _Implementierung von Teilaufgabe 2 und Teilaufgabe 3_ abzulegen (daher Sourcecode, Konfigurationen, etc.). Wählen Sie hierzu, nach dem Clonen dieses Repositories, diesen Ordner als Eclipse Workspace. Kopieren Sie das bereitgestellte Beispielprojekt (siehe Angabe auf Moodle) in diese. Danach das Readme im Beispielprojekt anwenden um das Projekt in Eclipse zu importieren. 

   _Vorgezeigt_ wird dieser Ablauf in den _Screencasts zu Eclipse/IDE und Git/GitLab_ auf Moodle. Hinsichtlich der zu verwendeten Pfade, Ordnernamen, Struktur, Projektbestandteile etc. in Source bzw. Abgaben der Teilaufgaben die Code umfassen die jeweilige Angabe auf Moodle beachten. Diese genau einzuhalten ist wichtig, da ansonsten der automatische Download und Upload zu Evaluierung durch die LV-Leitung fehlschlagen könnte. 

# Was gilt es während der Implementierung zu beachten? 

* **Beispielprojekte**: Sehen Sie sich die auf Moodle bereitgestellten Beispielprojekte an (für Teilaufgabe 2 und 3). Sie können diese mittels Eclipse einfach in den Eclipse-Workspace als Gradle-Projekt importieren und direkt mit der Implementierung beginnen. Um die Beispielprojekte einfach in Git bzw. GitLab zu integrieren zuerst das Repository clonen und anschließend, vor dem Eclipse-Import, in den passenden Source-Unterordner kopieren.
* **Vor einer Deadline**: Während den Bewertungen wird, als Erstes, der Inhalt des Ordners Source von den LV Leitern automatisiert heruntergeladen, mittels Gradle gebaut und das so entstehende Jar automatisiert zur grundlegenden Funktionalitäts-Bewertung herangezogen. Anschließend wird noch Ihr Projekt in Eclipse importiert und dort genauer analysiert hinsichtlich nicht automatisch prüfbarer Inhalte (z.B. bezüglich besprochener Best Practices). 
   Prüfen Sie daher sicherheitshalber ob dies fehlerfrei möglich ist indem Sie dieses Repository neu klonen, in einen neuen Eclipse Workspace importieren und anschließen Ihre Projekte bauen bzw. mittels Gradle in ein ausführbares Jar exportieren. Prüfen Sie anschließend dieses Jar mehrfach mit den bereitgestellten Lösungen zur Selbstevaluation.
* **Während der Bearbeitung**: Erstellen Sie keine zusätzlichen Ordner im Wurzelverzeichnis dieses Repositories und verändern Sie nicht die Namen, etc. der vorgegebenen Ordner. Zusätzliche Ordner, z.B. für Entwürfe, können Sie als Unterordner in den vorgegebenen Ordnern erstellen. Besser, bzw. mehr dem Stil von Git folgend, wäre es jedoch eigene *Branches* (siehe auch, branch-based development) zu nützen. 
   Achten Sie darauf, dass sich die finale Abgabe an der in Moodle vorgegebenen Stelle befindet. Stellen Sie sicher, dass nicht mehrere unterschiedliche/widersprüchliche Versionen Ihrer Abgaben in diesem Repository enthalten sind. Es muss während der Bewertung schnell und einfach möglich sein zu erkennen wo sich Ihre Abgabe befindet und welche Inhalte für die jeweilige Teilaufgabe relevant sind. 
   Andernfalls kann eine Situation entstehen, bei welcher versehentlich, beispielsweise, eine veraltete Kopie zur Bewertung herangezogen wird und Sie deshalb nicht alle Punkte erhalten. Eine spätere Korrektur und individuelle Nachfragen von unserer Seite sind Aufgrund der hohen Zahl an Studierenden organisatorisch nicht möglich.    

# Wie kann ich während der Implementierung Unterstützung erhalten?

Diese Lehrveranstaltung bietet auf vielen Ebenen Unterstützung an – wählen Sie nach Ihren Bedürfnissen. Beispielsweise wird für jedes für Sie neues Thema (z.B. Architektur, Netzwerk, Testing, Übungsangaben, usw.) ein Tutorial abgehalten, Beispielcode bereitgestellt und ein zugehöriges Skriptum auf Moodle hinterlegt. In den Vorbesprechungsfolien finden Sie eine Übersicht darüber, wann welches Thema behandelt wird. Gerne können Sie bei diesen Terminen auch zu themenfremden Bereichen Fragen stellen. Speziell dafür werden auch noch zusätzliche offene Fragestunden angeboten.

## Unterstützung außerhalb der Tutorials:

- Für **allgemeine Fragen**, von welchen Sie annehmen, dass diese für Sie und andere Studierende relevant sind empfehlen wir das jeweils passendste Moodle-Forum nützen. Dort können Sie auch mit anderen Studierenden diskutieren und sich gegenseitig helfen oder alte Fragen einsehen. Vielleicht findet sich genau Ihr Anliegen bereits dort.

- Für **spezifische Implementierungsfragen** empfehlen wir hier einen Git Issue zu erstellen. Beschreiben Sie darin Ihr Anliegen und - **wichtig** - vermerken Sie unseren Tutor. Hierzu dessen Git Handle im Issue Text, als Teil der Anfrage (inkl. @), einfügen. 

   Inhaltlich zielt diese Unterstützungsmöglichkeit darauf ab Sie bei für Sie **neuen** Themen in die Richtige Richtung zu lenken. Beispielsweise wenn die Netzwerkimplementierung nicht und nicht funktionieren will oder Sie trotz eigener Analysen nicht nachvollziehen können warum einer der Integrationstests nun genau einen Fehler in Ihrer Implementierung meldet. 

   Folgende Tutoren sind dieses Semester verfügbar: 

   - `Simon Eckerstorfer (Git Handle @simone99)`

- **Außerhalb von GitLab/Forum** können Sie uns per E-Mail erreichen. Den Tutor unter [email](mailto:tutor.swe1@univie.ac.at) sowie die LV-Leitung unter [email](mailto:swe1.wst@univie.ac.at). Für schnelle Antworten immer an diese LV-spezifischen E-Mail-Adressen schreiben und nicht an persönliche Adressen.

# Welche Funktionen sollen nicht genutzt werden?

GitLab ist eine mächtige Software, die es erlaubt zahlreiche Einstellungen anzupassen. Wir würden dazu raten diese Möglichkeiten nicht unüberlegt zu nützen da unbedachte Aktionen (z.B. das Löschen des Masterbranches) hierbei auch negative Auswirkungen haben können da GitLab teilweise nicht nachfragt, sondern Aktionen einfach ausgeführt (*Think before you click!*). Verwenden Sie daher optimalerweise einfach die vorgegebenen Einstellungen. Zur Sicherheit wurden, soweit möglich, unnötige Funktionen von uns bereits deaktiviert.
