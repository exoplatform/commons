package org.exoplatform.software.register.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.info.PlatformInformationRESTService;
import org.exoplatform.commons.info.PlatformInformationRESTService.JsonPlatformInfo;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.software.register.UnlockService;
import org.exoplatform.software.register.Utils;
import org.exoplatform.software.register.model.SoftwareRegistration;
import org.exoplatform.software.register.service.SoftwareRegistrationService;

/**
 * Created by The eXo Platform SEA Author : eXoPlatform toannh@exoplatform.com
 * On 9/30/15 Implement methods of SoftwareRegistrationService interface
 */
public class SoftwareRegistrationServiceImpl implements SoftwareRegistrationService {

  private static final Log               LOG                       = ExoLogger.getLogger(SoftwareRegistrationServiceImpl.class);

  private static final String            SW_NODE_NAME              = "SoftwareRegistration";

  private static boolean                 hasSoftwareRegisteredNode = false;

  private SettingService                 settingService;

  private PlatformInformationRESTService platformInformationRESTService;

  private InitParams                     initParams;

  private String                         softwareRegistrationHost  = SOFTWARE_REGISTRATION_HOST_DEFAULT;

  private UnlockService                  unlockService;

  private boolean                        isRequestSkip;

  private int                            skipedNum                 = 0;

  private String                         currStatus;

  private String                         currVersions;

  public SoftwareRegistrationServiceImpl(SettingService settingService,
                                         PlatformInformationRESTService platformInformationRESTService,
                                         InitParams initParams,
                                         UnlockService unlockService) {
    this.settingService = settingService;
    this.platformInformationRESTService = platformInformationRESTService;
    this.initParams = initParams;
    if (initParams != null && initParams.getValueParam(SOFTWARE_REGISTRATION_HOST) != null) {
      this.softwareRegistrationHost = initParams.getValueParam(SOFTWARE_REGISTRATION_HOST).getValue();
    }
    this.unlockService = unlockService;
    this.currStatus = Utils.readFromFile(Utils.SW_REG_STATUS, Utils.HOME_CONFIG_FILE_LOCATION);
    this.currVersions = Utils.readFromFile(platformInformationRESTService.getPlatformEdition()
                                                                         .concat("-")
                                                                         .concat(Utils.SW_REG_PLF_VERSION),
                                           Utils.HOME_CONFIG_FILE_LOCATION);

    try {
      skipedNum = Integer.parseInt(initParams.getValueParam(SOFTWARE_REGISTRATION_SKIP_ALLOW).getValue());
    } catch (NumberFormatException nfe) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("Skip allow configuration of PLF registration has been ignored!");
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSkipPlatformRegistration() {
    String skipPLFRegister = initParams.getValueParam(SOFTWARE_REGISTRATION_SKIP).getValue();
    if (skipPLFRegister == null)
      return false;
    return StringUtils.equals("true", skipPLFRegister.trim());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SoftwareRegistration registrationPLF(String code, String returnURL) {
    String url = softwareRegistrationHost + "/portal/accessToken";
    SoftwareRegistration softwareRegistration = new SoftwareRegistration();
    try {
      HttpClient client = new DefaultHttpClient();
      HttpPost post = new HttpPost(url);
      List<NameValuePair> urlParameters = new ArrayList<>();
      urlParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
      urlParameters.add(new BasicNameValuePair("code", code));
      urlParameters.add(new BasicNameValuePair("redirect_uri", returnURL));
      urlParameters.add(new BasicNameValuePair("client_id", "x6iCo6YWmw"));
      urlParameters.add(new BasicNameValuePair("client_secret", "3XNzbpuTSx5HqJsBSwgl"));

      post.setEntity(new UrlEncodedFormEntity(urlParameters));
      HttpResponse response = client.execute(post);
      BufferedReader rd = new BufferedReader(
                                             new InputStreamReader(response.getEntity().getContent()));
      StringBuffer result = new StringBuffer();
      String line = "";
      while ((line = rd.readLine()) != null) {
        result.append(line);
      }

      JSONObject responseData = new JSONObject(result.toString());
      if (response.getStatusLine().getStatusCode() == HTTPStatus.OK) {
        String accessToken = responseData.getString("access_token");
        softwareRegistration.setAccess_token(accessToken);
        boolean pushInfo = sendPlfInformation(accessToken);
        softwareRegistration.setPushInfo(pushInfo);
      } else {
        String errorCode = responseData.getString("error");
        softwareRegistration.setError_code(errorCode);
      }

      return softwareRegistration;
    } catch (Exception ex) {
      softwareRegistration.setNotReachable(true);
    }
    return softwareRegistration;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateSkippedNumber() {
    int skippedNumber = getSkippedNumber();
    settingService.set(Context.GLOBAL,
                       Scope.GLOBAL,
                       SOFTWARE_REGISTRATION_SKIPPED,
                       new SettingValue<Object>(String.valueOf(++skippedNumber)));
  }

  @Override
  public boolean canSkipRegister() {
    int skipedNum_ = getSkippedNumber();
    return skipedNum_ <= skipedNum || unlockService.isUnlocked();
  }

  @Override
  public boolean canShowSkipBtn() {
    int skipedNum_ = getSkippedNumber();
    return skipedNum_ < skipedNum || unlockService.isUnlocked();
  }

  private int getSkippedNumber() {
    SettingValue<?> settingValue = settingService.get(Context.GLOBAL, Scope.GLOBAL, SOFTWARE_REGISTRATION_SKIPPED);
    if (settingValue != null) {
      return Integer.parseInt(settingValue.getValue().toString());
    }
    settingService.set(Context.GLOBAL, Scope.GLOBAL, SOFTWARE_REGISTRATION_SKIPPED, new SettingValue<Object>("0"));
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSoftwareRegistered() {
    // Check plf registration on local
    if (StringUtils.isEmpty(currStatus) || StringUtils.isEmpty(currVersions))
      return false;
    boolean plfRegistrationStatus = currStatus.contains(platformInformationRESTService.getPlatformEdition().concat("-true"));
    boolean plfVersionRegistrationStatus = currVersions.contains(platformInformationRESTService.getJsonPlatformInfo()
                                                                                               .getPlatformVersion());
    return plfRegistrationStatus && plfVersionRegistrationStatus;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void checkSoftwareRegistration() {
    // Persisted registration status on local
    String currentRegStatus = Utils.readFromFile(Utils.SW_REG_STATUS, Utils.HOME_CONFIG_FILE_LOCATION);
    if (StringUtils.isEmpty(currentRegStatus)) {
      currentRegStatus = platformInformationRESTService.getPlatformEdition().concat("-true");
    } else if (!currentRegStatus.contains(platformInformationRESTService.getPlatformEdition().concat("-true"))) {
      currentRegStatus = currentRegStatus.concat(",").concat(platformInformationRESTService.getPlatformEdition().concat("-true"));
    }
    Utils.writeToFile(Utils.SW_REG_STATUS, currentRegStatus, Utils.HOME_CONFIG_FILE_LOCATION);
    this.currStatus = currentRegStatus;

    String plfVersionsKey = platformInformationRESTService.getPlatformEdition().concat("-").concat(Utils.SW_REG_PLF_VERSION);
    String plfVersions = Utils.readFromFile(plfVersionsKey, Utils.HOME_CONFIG_FILE_LOCATION);
    if (StringUtils.isEmpty(plfVersions)) {
      plfVersions = platformInformationRESTService.getJsonPlatformInfo().getPlatformVersion();
    } else if (!plfVersions.contains(platformInformationRESTService.getJsonPlatformInfo().getPlatformVersion())) {
      plfVersions = plfVersions.concat(",").concat(platformInformationRESTService.getJsonPlatformInfo().getPlatformVersion());
    }
    Utils.writeToFile(platformInformationRESTService.getPlatformEdition().concat("-").concat(Utils.SW_REG_PLF_VERSION),
                      plfVersions,
                      Utils.HOME_CONFIG_FILE_LOCATION);
    this.currVersions = plfVersions;
  }

  /**
   * {@inheritDoc}
   */
  private boolean sendPlfInformation(String accessTokencode) {
    try {
      String url = softwareRegistrationHost + "/portal/rest/registerSoftware/register";
      HttpClient client = new DefaultHttpClient();
      HttpPost httpPost = new HttpPost(url);

      JsonPlatformInfo jsonPlatformInfo = platformInformationRESTService.getJsonPlatformInfo();
      JSONObject jsonObj = new JSONObject(jsonPlatformInfo);

      String input = jsonObj.toString();

      httpPost.setHeader("Accept", "application/json");
      httpPost.setHeader("Content-type", "application/json");
      httpPost.setHeader("Authorization", "Bearer " + accessTokencode);
      httpPost.setEntity(new StringEntity(input));

      HttpResponse response = client.execute(httpPost);

      if (response.getStatusLine().getStatusCode() != 200) {
        LOG.warn("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
        return false;
      }
      return true;
    } catch (Exception e) {
      LOG.warn("Can not send Platform information to eXo community", e);
      return false;
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getSoftwareRegistrationHost() {
    return softwareRegistrationHost;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRequestSkip() {
    return isRequestSkip;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRequestSkip(boolean isRequestSkip) {
    this.isRequestSkip = isRequestSkip;
  }

}
