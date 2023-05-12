package jetbrains.buildServer.auth.saml.plugin;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

public class SamlCallbackController extends BaseController {

    private final Logger LOG = Loggers.AUTH;

    public SamlCallbackController(@NotNull SBuildServer server,
                                  @NotNull WebControllerManager webControllerManager
                                  ) {
        super(server);

        webControllerManager.registerController(SamlPluginConstants.SAML_CALLBACK_URL, this);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        LOG.debug(String.format("SAML callback initiated at %s", request.getRequestURL()));
        String relayState = request.getParameter("RelayState");
        Loggers.SERVER.warn("-----------");
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            Loggers.SERVER.warn(request.getParameter(parameterNames.nextElement()));
        }
        Loggers.SERVER.warn("-----------");
        if (relayState != null) {
            return new ModelAndView(new RedirectView(relayState));
        }
        return new ModelAndView(new RedirectView("/"));
    }
}
