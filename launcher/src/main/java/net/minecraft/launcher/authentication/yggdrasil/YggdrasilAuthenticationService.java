package net.minecraft.launcher.authentication.yggdrasil;
 
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import net.minecraft.launcher.Http;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.Util;
import net.minecraft.launcher.authentication.BaseAuthenticationService;
import net.minecraft.launcher.authentication.GameProfile;
import net.minecraft.launcher.authentication.exceptions.AuthenticationException;
import net.minecraft.launcher.authentication.exceptions.InvalidCredentialsException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
 
public class YggdrasilAuthenticationService extends BaseAuthenticationService
{
  private static final String BASE_URL = "https://authserver.mojang.com/";
  private static final URL ROUTE_AUTHENTICATE = Util.constantURL("https://authserver.mojang.com/authenticate");
  private static final URL ROUTE_REFRESH = Util.constantURL("https://authserver.mojang.com/refresh");
  private static final URL ROUTE_VALIDATE = Util.constantURL("https://authserver.mojang.com/validate");
  private static final URL ROUTE_INVALIDATE = Util.constantURL("https://authserver.mojang.com/invalidate");
  private static final URL ROUTE_SIGNOUT = Util.constantURL("https://authserver.mojang.com/signout");
  private static final String STORAGE_KEY_ACCESS_TOKEN = "accessToken";
  private final Gson gson = new Gson();
  private final Agent agent = Agent.MINECRAFT;
  private GameProfile[] profiles = null;
  private String accessToken = null;
  private boolean isOnline = false;
 
  public boolean canLogIn()
  {
    return (!canPlayOnline()) && (StringUtils.isNotBlank(getUsername())) && ((StringUtils.isNotBlank(getPassword())) || (StringUtils.isNotBlank(getAccessToken())));
  }
 
  public void logIn() throws AuthenticationException
  {
    if (StringUtils.isBlank(getUsername())) {
      throw new InvalidCredentialsException("Invalid username");
    }
    logInWithPassword();
  }
 
  protected void logInWithPassword()
    throws AuthenticationException
  {
    setSelectedProfile(new GameProfile("id", getUsername()));
    this.accessToken = "AccessToken";
    fireAuthenticationChangedEvent();
  }
 
  protected void logInWithToken() throws AuthenticationException
  {
    fireAuthenticationChangedEvent();
  }
 
  protected <T extends Response> T makeRequest(URL url, Object input, Class<T> classOfT) throws AuthenticationException {
    try {
      String jsonResult = Util.performPost(url, this.gson.toJson(input), Launcher.getInstance().getProxy(), "application/json", true);
      Response result = (Response)this.gson.fromJson(jsonResult, classOfT);
 
      if (result == null) return null;
 
      if (StringUtils.isNotBlank(result.getError())) {
        if (result.getError().equals("ForbiddenOperationException")) {
          throw new InvalidCredentialsException(result.getErrorMessage());
        }
        throw new AuthenticationException(result.getErrorMessage());
      }
 
      this.isOnline = true;
 
      return null;
      }
    catch (IOException e) {
    }
    throw new AuthenticationException("Cannot contact authentication server");
  }
 
  public void logOut()
  {
    super.logOut();
 
    if ((StringUtils.isNotBlank(getClientToken())) && (StringUtils.isNotBlank(getAccessToken()))) {
      Launcher.getInstance().println("Invalidating accessToken with server...");
      try
      {
        makeRequest(ROUTE_INVALIDATE, new InvalidateRequest(this), Response.class);
      } catch (AuthenticationException e) {
        Launcher.getInstance().println("Couldn't invalidate token on server", e);
      }
    }
 
    this.accessToken = null;
    this.profiles = null;
    this.isOnline = false;
  }
 
  public GameProfile[] getAvailableProfiles()
  {
    return this.profiles;
  }
 
  public boolean isLoggedIn()
  {
    return StringUtils.isNotBlank(this.accessToken);
  }
 
  public boolean canPlayOnline()
  {
    return (isLoggedIn()) && (getSelectedProfile() != null) && (this.isOnline);
  }
 
  public void selectGameProfile(GameProfile profile) throws AuthenticationException
  {
    if (!isLoggedIn()) {
      throw new AuthenticationException("Cannot change game profile whilst not logged in");
    }
    if (getSelectedProfile() != null) {
      throw new AuthenticationException("Cannot change game profile. You must log out and back in.");
    }
    if ((profile == null) || (!ArrayUtils.contains(this.profiles, profile))) {
      throw new IllegalArgumentException("Invalid profile '" + profile + "'");
    }
 
    RefreshRequest request = new RefreshRequest(this, profile);
    RefreshResponse response = (RefreshResponse)makeRequest(ROUTE_REFRESH, request, RefreshResponse.class);
 
    if (!response.getClientToken().equals(getClientToken())) {
      throw new AuthenticationException("Server requested we change our client token. Don't know how to handle this!");
    }
 
    this.accessToken = response.getAccessToken();
    setSelectedProfile(response.getSelectedProfile());
 
    fireAuthenticationChangedEvent();
  }
 
  public void loadFromStorage(Map<String, String> credentials)
  {
    super.loadFromStorage(credentials);
 
    this.accessToken = ((String)credentials.get("accessToken"));
  }
 
  public Map<String, String> saveForStorage()
  {
    Map result = super.saveForStorage();
    if (!shouldRememberMe()) return result;
 
    if (StringUtils.isNotBlank(getAccessToken())) {
      result.put("accessToken", getAccessToken());
    }
 
    return result;
  }
 
  public String getSessionToken()
  {
    if ((isLoggedIn()) && (getSelectedProfile() != null) && (canPlayOnline())) {
      return String.format("token:%s:%s", new Object[] { getAccessToken(), getSelectedProfile().getId() });
    }
    return null;
  }
 
  public String getAccessToken()
  {
    return this.accessToken;
  }
 
  public String getClientToken() {
    return Launcher.getInstance().getClientToken().toString();
  }
 
  public Agent getAgent() {
    return this.agent;
  }
 
  public String toString()
  {
    return "YggdrasilAuthenticationService{agent=" + this.agent + ", profiles=" + Arrays.toString(this.profiles) + ", selectedProfile=" + getSelectedProfile() + ", sessionToken='" + getSessionToken() + '\'' + ", username='" + getUsername() + '\'' + ", isLoggedIn=" + isLoggedIn() + ", canPlayOnline=" + canPlayOnline() + ", accessToken='" + this.accessToken + '\'' + ", clientToken='" + getClientToken() + '\'' + '}';
  }
} 