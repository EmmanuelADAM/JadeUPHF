package jade.imtp.socket;

import java.io.Serializable;

/**
 * Wire protocol for the socket IMTP: one request object goes out, one
 * response object comes back, over a plain TCP socket using standard Java
 * serialization. No RMI stubs/skeletons, no registry, no distributed GC.
 */
final class RpcProtocol {

    enum Target {
        NODE, PLATFORM_MANAGER
    }

    static final class Request implements Serializable {
        final Target target;
        final String method;
        final Object[] args;

        Request(Target target, String method, Object[] args) {
            this.target = target;
            this.method = method;
            this.args = args;
        }
    }

    static final class Response implements Serializable {
        final Object result;
        final Throwable exception;

        Response(Object result, Throwable exception) {
            this.result = result;
            this.exception = exception;
        }
    }

    private RpcProtocol() {
    }
}