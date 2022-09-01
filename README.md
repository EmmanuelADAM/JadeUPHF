# Jade version UPHF

Ce projet présente la mise à niveau de Jade par Emmnanuel ADAM (LAMIH/INSA HdF/UPHF), en bénéficiant d'aides d'étudiants de l'UPHF (Constant ABRAHAM & Théo BONHOMME).

Ce projet reprend donc la célèbre plateforme [Jade v4.5](https://jade.tilab.com/), stoppée en 2017, en la rendant compatible avec les versions récentes de Java (17).
Un très grand merci à ces développeurs pour leur important travail de qualité qui a permis à de très nombreuses personnes de développer des projets basés sur des SMA.

En plus de bénéficier d'un traitement plus sain et plus rapide grâce à l'utilisation des dernières classes de Java 
pour la gestion de listes, etc., des classes et fonctionnalités ont été ajoutées afin de simplifier la création 
d'agents et leurs communications.

*Des exemples utilisant cette nouvelle version de Jade se trouvent [ici](https://emmanueladam.github.io/jade/).*

**Adapter un code précédent.** Cette nouvelle version ne nécessite pas de modification de code, sauf pour la gestion des protocoles impliquant la réception/l'envoie de plusieurs messages (CFP, ...).
Alors, il suffit de remplacer les notions de `Vector` par des `List<ACLMessage>`.  
- Exemple, pour un `ContractNetInitiator`
    - `protected void handleAllResponses(Vector responses, Vector acceptances)`
  - devient
  - `protected void handleAllResponses(List<ACLMessage> responses, List<ACLMessage> acceptances)`
- Dans le protocole `AchieveRE`,
  - `protected void handleAllResultNotifications(Vector responses)` 
  - devient
  - `protected void handleAllResultNotifications(List<ACLMessage> responses)`
- ...

**Simplifier un code précédent.** Si vous avez développé avec la version Jade de 2017, le reste des codes fonctionne.
Cependant, vous pouvez simplifier ces codes. Ainsi : 
  - **Messages**
    - il est possible de designer plusieurs destinataires à un message (`addReceivers`), d'ajouter des destinataires 
      en passant le nom local en paramètre (`addReceiver("nameOfMyLocalFriend")`)
  - **Services**
    - une classe `AgentServiceTools` contient des fonctions utiles pour créer une description de services, s'enregistrer aurpès des pages jaunes, créer un canal radio (*topic*), ...
  - **Comportements**
    - les comportements permettent d'être initialisés avec des lambda expressions, principalement des Agent Consumer
        - `OneShotBehaviour`
            - un agent peut initialiser un OneShotBehaviour ainsi :
            - `addBehaviour(new OneShotBehaviour(this, 300, a->System.out.println("Just one hello from " + a.getLocalName
              ()));`
      - `TickerBehaviour`
          - un agent peut initialiser un TickerBehaviour ainsi :
          - `addBehaviour(new TickerBehaviour(this, 300, a->System.out.println("Hello, my name is " + a.getLocalName()));`
      - `ReceiverBehaviour`
        - ajout d'un comportement attendant indéfiniment (-1) un prochain message, sans préciser le template 
          (null) (utilisation d'un BiConsumer (Agent, ACLMessage)):
        - `addBehaviour(new ReceiverBehaviour(this, -1, null, (a, m)->{println(a.getLocalName() + ", I received " + 
          m.getContent() + " from " + m.getSender());}));`
        - ajout d'un comportement qui affiche chaque message reçu, indéfiniment (-1), sans 
          préciser le template (null) (il vaut mieux en préciser un!):
        - `addBehaviour(new ReceiverBehaviour(this, -1, null, true, (a, m)->{println(a.getLocalName() + ", I 
          received " + m.getContent() + " from " + m.getSender());}));`
    - **Interactions**
        - un Agent possède la fonction `println("texte")` qui affiche un texte sur la console
        - une classe `AgentWindowed` associe à un agent une petite fenêtre, utile pour la visualisation et le débogage, une fonction `void println(String text)` permet d'afficher une ligne sur celle-ci.
      Un bouton activable permet aussi une interaction simple avec l'utilisateur.

N'hésitez pas à vous référer aux [exemples](https://emmanueladam.github.io/jade/).

*(JADE is(was?) a free software and distributed by [Telecom Italia](https://www.gruppotim.it/it.html), the copyright holder, in open source under the terms and conditions of the LGPL ( Lesser General Public License Version 2) license.*


---
# Java Agent Development Framework, UPHF Version.

This project is an update of Jade platform by Emmnanuel ADAM (LAMIH/INSA HdF/UPHF), with the help of two students from UPHF (Constant ABRAHAM & Théo BONHOMME).


This project is a fork of the [Jade framework v4.5](https://jade.tilab.com/), stopped in 2017, motivated by the fact that the team that successfully developed it cannot continue to support the project.

A great thanks to the team that built Jade, and that have made a very nice and helpful work that allows a lot of people to develop multiagent systems.

*Examples using this new version of Jade can be found [here](https://emmanueladam.github.io/jade/).*

**To adapt a previous code.** This new version doesn't necessitate to modify your code, except for the use of protocols that implies reception/sending of list of messages (CFP, ...).
Then, you just have to replace  occurrence of `Vector` by `List<ACLMessage>`.
- Thus, for a `ContractNetInitiator`
  - `protected void handleAllResponses(Vector responses, Vector acceptances)`
  - becomes
  - `protected void handleAllResponses(List<ACLMessage> responses, List<ACLMessage> acceptances)`
- And in the protocol `AchieveRE`,
  - `protected void handleAllResultNotifications(Vector responses)`
  - becomes
  - `protected void handleAllResultNotifications(List<ACLMessage> responses)`
- ...

**To simplify a previous code.** If you have implemented some agents with the  version 2017 of Jade, except or the previous cases, the codes run with this new version.
However, you can simplify them. Thus :
- **Messages**
    - it is possible to define several receivers to a message (`addReceivers`), to add receivers with their local 
      names (`addReceiver("nameOfMyLocalFriend")`)
- **Services**
  - a class, `AgentServiceTools`, owns useful functions for create service description, register an agent to the yellow pages (DFAgent), create a radio canal (*topic*), ...
- **Comportements**
  - behaviours can be initialized with lambda expressions, mainly (Agent) Consumers :
  - `OneShotBehaviour`
    - an agent can initialize a OneShotBehaviour with :
      - `addBehaviour(new OneShotBehaviour(this, 300, a->System.out.println("Just one hello from " + a.getLocalName
              ()));`
  - `TickerBehaviour`
    - an agent can initialize a OneShotBehaviour with :
      - `addBehaviour(new TickerBehaviour(this, 300, a->System.out.println("Hello, my name is " + a.getLocalName()));`
  - `ReceiverBehaviour`
      - add a behaviour that infinitely (-1) wait for one next message, whatever the template (null) (use of a BiConsumer (Agent, ACLMessage)):
      - `addBehaviour(new ReceiverBehaviour(this, -1, null, (a, m)->{println(a.getLocalName() + ", I received " +
        m.getContent() + " from " + m.getSender());}));`
      - add a behaviour that continuously wait for a message, whatever the template (null) (you should use one !) :
        - `addBehaviour(new ReceiverBehaviour(this, -1, null, true, (a, m)->{println(a.getLocalName() + ", I
          received " + m.getContent() + " from " + m.getSender());}));`
  - **Interactions**
      - an Agent owns the function `println("text")` that displays a text on the console
      - a class, `AgentWindowed`, link a small window to an agent, useful for the control and the debug, a function 
        `void println(String text)` add a text line on it. 
      - An activable button allows a simple interaction with the user.

Feel free to have a look to the (french)  [samples](https://emmanueladam.github.io/jade/).

*(JADE is(was?) a free software and distributed by [Telecom Italia](https://www.gruppotim.it/it.html), the copyright holder, in open source under the terms and conditions of the LGPL ( Lesser General Public License Version 2) license.*
