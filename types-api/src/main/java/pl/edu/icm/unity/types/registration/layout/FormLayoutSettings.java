/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.types.registration.layout;

import java.util.Objects;

import pl.edu.icm.unity.types.registration.BaseForm;

/**
 * Provides visual configuration that can be applied on a {@link BaseForm}.
 *
 * @author Roman Krysinski (roman@unity-idm.eu)
 */
public class FormLayoutSettings
{
	public static final FormLayoutSettings DEFAULT = new FormLayoutSettings(false, 21, "EM");

	private boolean compactInputs;
	private float columnWidth;
	private String columnWidthUnit;

	protected FormLayoutSettings()
	{
	}

	public FormLayoutSettings(boolean compactInputs, float columnWidth, String columnWidthUnit)
	{
		this.compactInputs = compactInputs;
		this.columnWidth = columnWidth;
		this.columnWidthUnit = columnWidthUnit;
	}

	public boolean isCompactInputs()
	{
		return compactInputs;
	}

	public void setCompactInputs(boolean compactInputs)
	{
		this.compactInputs = compactInputs;
	}

	public float getColumnWidth()
	{
		return columnWidth;
	}

	public void setColumnWidth(float columnWidth)
	{
		this.columnWidth = columnWidth;
	}

	public String getColumnWidthUnit()
	{
		return columnWidthUnit;
	}

	public void setColumnWidthUnit(String columnWidthUnit)
	{
		this.columnWidthUnit = columnWidthUnit;
	}

	@Override
	public boolean equals(final Object other)
	{
		if (!(other instanceof FormLayoutSettings))
			return false;
		FormLayoutSettings castOther = (FormLayoutSettings) other;
		return Objects.equals(compactInputs, castOther.compactInputs)
				&& Objects.equals(columnWidth, castOther.columnWidth)
				&& Objects.equals(columnWidthUnit, castOther.columnWidthUnit);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(compactInputs, columnWidth, columnWidthUnit);
	}
	
	public static Builder builder()
	{
		return new Builder();
	}
	
	public static class Builder
	{
		private boolean compactInputs;
		private float columnWidth;
		private String columnWidthUnit;
		
		public Builder withCompactInputs(boolean compactInputs)
		{
			this.compactInputs = compactInputs;
			return this;
		}
		public Builder withColumnWidth(float columnWidth)
		{
			this.columnWidth = columnWidth;
			return this;
		}
		public Builder withColumnWidthUnit(String unit)
		{
			this.columnWidthUnit = unit;
			return this;
		}
		public FormLayoutSettings build()
		{
			return new FormLayoutSettings(compactInputs, columnWidth, columnWidthUnit);
		}
		
	}
}