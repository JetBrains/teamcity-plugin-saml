package jetbrains.buildServer.web;

import jetbrains.buildServer.auth.saml.plugin.InMemorySamlPluginSettingsStorage;
import jetbrains.buildServer.auth.saml.plugin.SamlAuthenticationScheme;
import jetbrains.buildServer.auth.saml.plugin.SamlPluginConstants;
import lombok.var;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SamlCsrfCheckTest {

    private SamlCsrfCheck check;
    private SamlAuthenticationScheme scheme;

    @Before
    public void setUp() throws Exception {
        Mockito.reset();
        this.scheme = mock(SamlAuthenticationScheme.class);
        when(this.scheme.isConfigured()).thenReturn(true);
        when(this.scheme.getCallbackUrl(null)).thenReturn(new URL("http://someurl.local"));

        InMemorySamlPluginSettingsStorage settingsStorage = new InMemorySamlPluginSettingsStorage();

        this.check = new SamlCsrfCheck(this.scheme, settingsStorage);
    }

    @Test
    public void whenSAMLRequestIsMadeToCallbackURLItIsSafe() throws MalformedURLException {
        var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        StringBuffer URL = new StringBuffer(this.scheme.getCallbackUrl(null).toString());
        when(request.getRequestURL()).thenReturn(URL);
        when(request.getParameter(SamlPluginConstants.SAML_RESPONSE_REQUEST_PARAMETER)).thenReturn("SAMLResponse=1");

        CsrfCheck.CheckResult result = this.check.isSafe(request);

        assertThat(result.isSafe(), equalTo(true));
    }

    @Test
    public void shouldBeTrailingSlashAgnostic() throws MalformedURLException {
        var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        String requestUrl = "https://somesite.local/app/callback";
        String callbackUrl = requestUrl + "/";
        when(this.scheme.getCallbackUrl(null)).thenReturn(new URL(callbackUrl));
        when(request.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
        when(request.getParameter(SamlPluginConstants.SAML_RESPONSE_REQUEST_PARAMETER)).thenReturn("SAMLResponse=1");

        CsrfCheck.CheckResult result = this.check.isSafe(request);

        assertThat(result.isSafe(), equalTo(true));

        callbackUrl = requestUrl;
        requestUrl = callbackUrl + "/";
        when(request.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
        when(this.scheme.getCallbackUrl(null)).thenReturn(new URL(callbackUrl));
        result = this.check.isSafe(request);
        assertThat(result.isSafe(), equalTo(true));
    }

    @Test
    public void shouldNotFireWhenSchemeIsDisabled() {
        when(this.scheme.isConfigured()).thenReturn(false);
        var request = mock(HttpServletRequest.class);

        assertThat(this.check.isSafe(request).isSafe(), equalTo(false));
    }
}
