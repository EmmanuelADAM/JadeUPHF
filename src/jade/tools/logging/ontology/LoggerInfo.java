package jade.tools.logging.ontology;

import jade.content.Concept;

import java.util.List;

public class LoggerInfo implements Concept {
    private String name;
    private int level;
    private List<?> handlers;
    private String file;

    public LoggerInfo() {

    }

    public LoggerInfo(String name, int level) {
        setName(name);
        setLevel(level);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<?> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<?> handlers) {
        this.handlers = handlers;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String toString() {
        return name;
    }
}
