package jade.tools.logging.ontology;

//#J2ME_EXCLUDE_FILE

import jade.content.AgentAction;

public class GetAllLoggers implements AgentAction {
    private String type;
    private String filter;

    public GetAllLoggers() {
    }

    public GetAllLoggers(String type, String filter) {
        this.type = type;
        this.filter = filter;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
