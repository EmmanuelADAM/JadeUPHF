# Modern monitoring tools

Plain-Swing, drop-in replacements for the two JADE monitoring windows most
projects end up opening, whose late-90s look ages the whole platform:
`jade.tools.DummyAgent.DummyAgent` and `jade.tools.sniffer.Sniffer`.

No new dependency is added: everything here is plain `javax.swing`, built
only against the rest of this library, and it ships inside `JadeUPHF.jar`
like every other package.

## What's inside

| Package | Content |
|---|---|
| `monitoring.ui` | Shared, reusable modern Swing components: `ModernTheme` (light/dark palette), `ModernButton`, `RoundedPanel`, `ModernScrollBarUI`, `StatusPill`, `PerformativeColors`, `ModernLogPanel`, `SequenceCanvas`. Free to reuse in your own agent GUIs. |
| `monitoring.agents.ModernDummyAgent` + `monitoring.gui.ModernDummyAgentGui` | Replacement for `jade.tools.DummyAgent.DummyAgent`: a full ACL message composer (performative, receivers, reply-to, language, encoding, ontology, protocol, conversation-id, in-reply-to, reply-with, content), a live sent/received history with Reply, Edit (set as current) and Delete, and Open/Save for both a single message and the whole history. |
| `monitoring.agents.ModernSnifferAgent` + `monitoring.gui.ModernSnifferGui` | Replacement for `jade.tools.sniffer.Sniffer`: a live, filterable message table plus a sequence-diagram view (one lifeline per agent, one arrow per message), colored by performative, with Save log to file. Can watch an agent with **zero code changes** (see below) as well as agents that report themselves in-process. |
| `monitoring.MessageBus` / `monitoring.Monitor` | The in-JVM publish/subscribe bus behind the Sniffer: fed either by the real platform `SniffOn` mechanism or by an agent explicitly reporting its own messages. |
| `monitoring.io.MessageLogFile` | Save/load of a single ACL message or a whole log to/from a plain text file, used by both tools. |

## Launching them

Both tools are plain agent classes, so they slot into any `Profile.AGENTS`
string exactly like the historical tools did:

```java
prop.setProperty(Profile.AGENTS,
    "myDummy:monitoring.agents.ModernDummyAgent;"
  + "mySniffer:monitoring.agents.ModernSnifferAgent");
```

## Watching an agent with zero code changes

Type an agent's local name in the Sniffer's "Watch an agent" field and
click **Watch**: this sends the real FIPA/JADE `SniffOn` request to the AMS
- the exact mechanism `jade.tools.sniffer.Sniffer` itself uses - so the
platform starts forwarding a copy of every message that agent sends or
receives. **No change to that agent's code is needed**, and it works even
for agents you did not write, or agents running on a remote container.
Click the agent's chip (`name  ×`) to stop watching it (sends `SniffOff`).

## Plugging the Sniffer into your own agent (in-process alternative)

If you would rather not depend on the platform's AMS forwarding (or want to
watch an agent whose container the AMS cannot reach), an agent can report
its own messages directly: replace `send(msg)` with `Monitor.send(this, msg)`,
and report every message you `receive()`/`blockingReceive()` with
`Monitor.received(this, msg)`.

```java
import monitoring.Monitor;
// ...
Monitor.send(this, msg);          // instead of: send(msg);
```

```java
ACLMessage msg = receive(template);
if (msg != null) {
    Monitor.received(this, msg);  // add this line
    // ... your existing handling
}
```

## Known limitations

- The "Watch an agent" field resolves a local name on the sniffer's own
  platform; it does not (yet) offer a live tree of every agent/container
  currently running, unlike the original Sniffer's agent tree.
- If an agent is both watched via `SniffOn` and separately calls
  `Monitor.send`/`Monitor.received` itself, the same message can be
  reported twice.
- The Dummy Agent composer covers every field the historical `AclGui`
  exposes except the reply-by date and the raw envelope tab (comments,
  ACL representation, payload encoding, ...), and there is no per-agent
  mobility GUI lifecycle (dispose/restore on move/freeze).

## Runnable examples

Runnable demo launchers (a Modern Dummy Agent, and a Modern Sniffer wired
to two instrumented demo agents) live in the tutorial repository, not in
this library: [emmanueladam/jade, `monitoring/launch`](https://github.com/EmmanuelADAM/jade/tree/english/monitoring/launch).
