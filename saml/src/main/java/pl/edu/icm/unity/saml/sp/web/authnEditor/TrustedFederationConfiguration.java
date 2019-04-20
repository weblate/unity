/*
 * Copyright (c) 2019 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package pl.edu.icm.unity.saml.sp.web.authnEditor;

import java.util.Properties;

import pl.edu.icm.unity.Constants;
import pl.edu.icm.unity.engine.api.translation.TranslationProfileGenerator;
import pl.edu.icm.unity.exceptions.InternalException;
import pl.edu.icm.unity.saml.SamlProperties;
import pl.edu.icm.unity.saml.sp.SAMLSPProperties;
import pl.edu.icm.unity.saml.sp.SAMLSPProperties.MetadataSignatureValidation;
import pl.edu.icm.unity.types.translation.TranslationProfile;

/**
 * SAML trusted federation configuration
 * @author P.Piernik
 *
 */
public class TrustedFederationConfiguration
{
	private String name;
	private String url;
	private String httpsTruststore;
	private boolean ignoreSignatureVerification;
	private String signatureVerificationCertificate;
	private int refreshInterval;
	private String registrationForm;
	private TranslationProfile translationProfile;

	public TrustedFederationConfiguration()
	{
		setRefreshInterval(SamlProperties.DEFAULT_METADATA_REFRESH);
		setIgnoreSignatureVerification(true);
		setTranslationProfile(TranslationProfileGenerator.generateEmptyInputProfile());
	}

	public void fromProperties(SAMLSPProperties source, String name)
	{

		setName(name);
		String prefix = SAMLSPProperties.IDPMETA_PREFIX + name + ".";

		setUrl(source.getValue(prefix + SamlProperties.METADATA_URL));
		setHttpsTruststore(source.getValue(prefix + SamlProperties.METADATA_HTTPS_TRUSTSTORE));

		if (source.isSet(prefix + SamlProperties.METADATA_SIGNATURE))
		{
			setIgnoreSignatureVerification(source
					.getEnumValue(prefix + SamlProperties.METADATA_SIGNATURE,
							MetadataSignatureValidation.class)
					.equals(MetadataSignatureValidation.ignore));
		}

		setSignatureVerificationCertificate(source.getValue(prefix + SamlProperties.METADATA_ISSUER_CERT));
		if (source.isSet(prefix + SamlProperties.METADATA_REFRESH))
		{
			setRefreshInterval(source.getIntValue(prefix + SamlProperties.METADATA_REFRESH));
		}
		setRegistrationForm(source.getValue(prefix + SAMLSPProperties.IDPMETA_REGISTRATION_FORM));

		if (source.isSet(prefix + SAMLSPProperties.IDPMETA_EMBEDDED_TRANSLATION_PROFILE))
		{
			setTranslationProfile(TranslationProfileGenerator.getProfileFromString(source
					.getValue(prefix + SAMLSPProperties.IDPMETA_EMBEDDED_TRANSLATION_PROFILE)));

		} else if (source.isSet(prefix + SAMLSPProperties.IDPMETA_TRANSLATION_PROFILE))
		{
			setTranslationProfile(TranslationProfileGenerator.generateIncludeInputProfile(
					source.getValue(prefix + SAMLSPProperties.IDPMETA_TRANSLATION_PROFILE)));
		}
	}

	public void toProperties(Properties raw)
	{
		String prefix = SAMLSPProperties.P + SAMLSPProperties.IDPMETA_PREFIX  + getName() + ".";

		raw.put(prefix + SamlProperties.METADATA_URL, getUrl());

		if (getHttpsTruststore() != null)
		{
			raw.put(prefix + SamlProperties.METADATA_HTTPS_TRUSTSTORE, getHttpsTruststore());
		}

		if (!isIgnoreSignatureVerification())
		{
			raw.put(prefix + SamlProperties.METADATA_SIGNATURE, MetadataSignatureValidation.require.toString());
		} else
		{
			raw.put(prefix + SamlProperties.METADATA_SIGNATURE, MetadataSignatureValidation.ignore.toString());
		}

		if (getSignatureVerificationCertificate() != null)
		{
			raw.put(prefix + SamlProperties.METADATA_ISSUER_CERT, getSignatureVerificationCertificate());
		}

		raw.put(prefix + SamlProperties.METADATA_REFRESH, String.valueOf(getRefreshInterval()));

		if (getRegistrationForm() != null)
		{
			raw.put(prefix + SAMLSPProperties.IDPMETA_REGISTRATION_FORM, getRegistrationForm());
		}

		try
		{
			raw.put(prefix + SAMLSPProperties.IDPMETA_EMBEDDED_TRANSLATION_PROFILE,
					Constants.MAPPER.writeValueAsString(getTranslationProfile().toJsonObject()));
		} catch (Exception e)
		{
			throw new InternalException("Can't serialize provider's translation profile to JSON", e);
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getHttpsTruststore()
	{
		return httpsTruststore;
	}

	public void setHttpsTruststore(String httpsTruststore)
	{
		this.httpsTruststore = httpsTruststore;
	}

	public boolean isIgnoreSignatureVerification()
	{
		return ignoreSignatureVerification;
	}

	public void setIgnoreSignatureVerification(boolean ignoreSignatureVerification)
	{
		this.ignoreSignatureVerification = ignoreSignatureVerification;
	}

	public String getSignatureVerificationCertificate()
	{
		return signatureVerificationCertificate;
	}

	public void setSignatureVerificationCertificate(String signatureVerificationCertificate)
	{
		this.signatureVerificationCertificate = signatureVerificationCertificate;
	}

	public int getRefreshInterval()
	{
		return refreshInterval;
	}

	public void setRefreshInterval(int refreshInterval)
	{
		this.refreshInterval = refreshInterval;
	}

	public String getRegistrationForm()
	{
		return registrationForm;
	}

	public void setRegistrationForm(String registrationForm)
	{
		this.registrationForm = registrationForm;
	}

	public TranslationProfile getTranslationProfile()
	{
		return translationProfile;
	}

	public void setTranslationProfile(TranslationProfile translationProfile)
	{
		this.translationProfile = translationProfile;
	}

	public TrustedFederationConfiguration clone()
	{
		TrustedFederationConfiguration clone = new TrustedFederationConfiguration();

		clone.setName(new String(this.getName()));

		clone.setUrl(this.getUrl() != null ? new String(this.getUrl()) : null);
		clone.setHttpsTruststore(
				this.getHttpsTruststore() != null ? new String(this.getHttpsTruststore()) : null);

		clone.setIgnoreSignatureVerification(new Boolean(this.ignoreSignatureVerification));

		clone.setSignatureVerificationCertificate(this.getSignatureVerificationCertificate() != null
				? new String(this.getSignatureVerificationCertificate())
				: null);

		clone.setRefreshInterval(new Integer(this.getRefreshInterval()));

		clone.setRegistrationForm(
				this.getRegistrationForm() != null ? new String(this.getRegistrationForm()) : null);
		clone.setTranslationProfile(
				this.getTranslationProfile() != null ? this.getTranslationProfile().clone() : null);
		return clone;
	}
}