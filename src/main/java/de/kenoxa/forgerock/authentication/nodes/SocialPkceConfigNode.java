/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2017-2018 ForgeRock AS.
 */


package de.kenoxa.forgerock.authentication.nodes;

import com.google.inject.assistedinject.Assisted;
import com.sun.identity.sm.RequiredValueValidator;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.forgerock.oauth.OAuthClientConfiguration;
import org.forgerock.oauth.clients.oauth2.OAuth2ClientConfiguration;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.Node.Metadata;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.nodes.oauth.AbstractSocialAuthLoginNode;
import org.forgerock.openam.auth.nodes.oauth.ProfileNormalizer;
import org.forgerock.openam.auth.nodes.oauth.SocialOAuth2Helper;
import org.forgerock.openam.sm.annotations.adapters.Password;
import org.forgerock.openam.sm.validation.URLValidator;
import org.forgerock.oauth.clients.oauth2.PkceMethod;

@Metadata(outcomeProvider = AbstractSocialAuthLoginNode.SocialAuthOutcomeProvider.class, configClass = SocialPkceConfigNode.SocialPkceConfigNodeConfig.class)
public class SocialPkceConfigNode extends AbstractSocialAuthLoginNode {
  public static interface SocialPkceConfigNodeConfig extends AbstractSocialAuthLoginNode.Config {
    @Attribute(order = 10)
    default PkceMethod pkceMethod() {
      return PkceMethod.NONE;
    }
    
    @Attribute(order = 100, validators = {RequiredValueValidator.class})
    String clientId();
    
    @Attribute(order = 200, validators = {RequiredValueValidator.class})
    @Password
    char[] clientSecret();
    
    @Attribute(order = 300, validators = {RequiredValueValidator.class, URLValidator.class})
    default String authorizeEndpoint() {
      return "";
    }
    
    @Attribute(order = 400, validators = {RequiredValueValidator.class, URLValidator.class})
    default String tokenEndpoint() {
      return "";
    }
    
    @Attribute(order = 500, validators = {RequiredValueValidator.class, URLValidator.class})
    default String userInfoEndpoint() {
      return "";
    }
    
    @Attribute(order = 600, validators = {RequiredValueValidator.class})
    default String scopeString() {
      return "openid profile email";
    }
    
    @Attribute(order = 700, validators = {RequiredValueValidator.class, URLValidator.class})
    default String redirectURI() {
      return SocialPkceConfigNode.getServerURL();
    }
    
    @Attribute(order = 800)
    default String provider() {
      return "social";
    }
    
    @Attribute(order = 900, validators = {RequiredValueValidator.class})
    default String authenticationIdKey() {
      return "sub";
    }
    
    @Attribute(order = 1000)
    default boolean basicAuth() {
      return true;
    }
    
    @Attribute(order = 1100, validators = {RequiredValueValidator.class})
    default String cfgAccountProviderClass() {
      return "org.forgerock.openam.authentication.modules.common.mapping.DefaultAccountProvider";
    }
    
    @Attribute(order = 1200, validators = {RequiredValueValidator.class})
    default String cfgAccountMapperClass() {
      return "org.forgerock.openam.authentication.modules.common.mapping.JsonAttributeMapper|*|social-";
    }
    
    @Attribute(order = 1300, validators = {RequiredValueValidator.class})
    default Set<String> cfgAttributeMappingClasses() {
      return Collections.singleton("org.forgerock.openam.authentication.modules.common.mapping.JsonAttributeMapper|iplanet-am-user-alias-list|social-");
    }
    
    @Attribute(order = 1400, validators = {RequiredValueValidator.class})
    default Map<String, String> cfgAccountMapperConfiguration() {
      return Collections.singletonMap("sub", "iplanet-am-user-alias-list");
    }
    
    @Attribute(order = 1500, validators = {RequiredValueValidator.class})
    default Map<String, String> cfgAttributeMappingConfiguration() {
      Map<String, String> attributeMappingConfiguration = new HashMap<>();
      attributeMappingConfiguration.put("sub", "iplanet-am-user-alias-list");
      attributeMappingConfiguration.put("given_name", "givenName");
      attributeMappingConfiguration.put("family_name", "sn");
      attributeMappingConfiguration.put("email", "mail");
      attributeMappingConfiguration.put("name", "cn");
      return attributeMappingConfiguration;
    }
    
    @Attribute(order = 1600)
    default boolean saveUserAttributesToSession() {
      return true;
    }
    
    @Attribute(order = 1700)
    default boolean cfgMixUpMitigation() {
      return false;
    }
    
    @Attribute(order = 1800)
    default String issuer() {
      return "";
    }
  }
  
  @Inject
  public SocialPkceConfigNode(@Assisted SocialPkceConfigNodeConfig config, SocialOAuth2Helper authModuleHelper, ProfileNormalizer profileNormalizer) throws NodeProcessException {
    super(config, authModuleHelper, authModuleHelper.newOAuthClient(getOAuthClientConfiguration(config)), profileNormalizer);
  }
  
  private static OAuthClientConfiguration getOAuthClientConfiguration(SocialPkceConfigNodeConfig config) {
    return ((OAuth2ClientConfiguration.Builder)((OAuth2ClientConfiguration.Builder)OAuth2ClientConfiguration.oauth2ClientConfiguration()
      .withPkceMethod(config.pkceMethod())
      .withClientId(config.clientId())
      .withClientSecret(new String(config.clientSecret()))
      .withAuthorizationEndpoint(config.authorizeEndpoint())
      .withTokenEndpoint(config.tokenEndpoint())
      .withScope(Collections.singletonList(config.scopeString()))
      .withScopeDelimiter(" ")
      .withBasicAuth(config.basicAuth())
      .withUserInfoEndpoint(config.userInfoEndpoint())
      .withRedirectUri(URI.create(config.redirectURI()))
      .withProvider(config.provider()))
      .withAuthenticationIdKey(config.authenticationIdKey())).build();
  }
}
