/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.webui.common.attributes.ext;

import com.vaadin.data.Binder;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload.SucceededEvent;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import pl.edu.icm.unity.base.utils.Log;
import pl.edu.icm.unity.engine.api.attributes.AttributeValueSyntax;
import pl.edu.icm.unity.engine.api.msg.UnityMessageSource;
import pl.edu.icm.unity.exceptions.IllegalAttributeTypeException;
import pl.edu.icm.unity.exceptions.IllegalAttributeValueException;
import pl.edu.icm.unity.stdext.attr.ImageAttributeSyntax;
import pl.edu.icm.unity.stdext.utils.BufferedImageWithExt;
import pl.edu.icm.unity.webui.common.*;
import pl.edu.icm.unity.webui.common.attributes.AttributeSyntaxEditor;
import pl.edu.icm.unity.webui.common.attributes.AttributeViewerContext;
import pl.edu.icm.unity.webui.common.attributes.WebAttributeHandler;
import pl.edu.icm.unity.webui.common.attributes.WebAttributeHandlerFactory;
import pl.edu.icm.unity.webui.common.attributes.edit.AttributeEditContext;
import pl.edu.icm.unity.webui.common.attributes.edit.AttributeValueEditor;
import pl.edu.icm.unity.webui.common.boundededitors.IntegerBoundEditor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

/**
 * Image attribute handler for the web
 *
 * @author R. Ledzinski
 */
public class ImageAttributeHandler implements WebAttributeHandler
{
	private static final Logger log = Log.getLogger(Log.U_SERVER_WEB, ImageAttributeHandler.class);
	private static final Random r = new Random();
	private static final int PREVIEW_WIDTH = 256;
	private static final int PREVIEW_HEIGHT = 128;
	private UnityMessageSource msg;
	private ImageAttributeSyntax syntax;

	public ImageAttributeHandler(UnityMessageSource msg, AttributeValueSyntax<?> syntax)
	{
		this.msg = msg;
		this.syntax = (ImageAttributeSyntax) syntax;
	}

	@Override
	public String getValueAsString(String value)
	{
		return "Image";
	}

	private Resource getValueAsImage(BufferedImageWithExt value,
									 ImageAttributeSyntax syntax, int maxWidth, int maxHeight)
	{
		try
		{
			BufferedImage scaled = scaleIfNeeded(value.getImage(), maxWidth, maxHeight);
			SimpleImageSource source = new SimpleImageSource(scaled, syntax, value.getType());
			return source.getResource();
		} catch (Exception e)
		{
			log.warn("Problem getting value's image as resource: " + e, e);
			return null;
		}
	}

	@Override
	public Component getRepresentation(String valueRaw, AttributeViewerContext context)
	{
		BufferedImageWithExt value = syntax.convertFromString(valueRaw);
		if (value == null)
			return  getErrorImage();
		
		int width = value.getImage().getWidth();
		int height = value.getImage().getHeight();
		Resource resValue = getValueAsImage(value, syntax, width,
				height);
		if (resValue != null)
		{
			Image image = new Image();

			image.setSource(resValue);
			return image;
		} else
		{
			return  getErrorImage();
		}	
	}
	
	private Label getErrorImage()
	{
		Label errorImage =  new Label(Images.error.getHtml());
		errorImage.setContentMode(ContentMode.HTML);
		errorImage.addStyleName(Styles.largeIcon.toString());
		return errorImage;
	}

	private BufferedImage scaleIfNeeded(BufferedImage value, int maxWidth, int maxHeight)
	{
		int w = value.getWidth();
		int h = value.getHeight();
		if (w<=maxWidth && h<=maxHeight)
			return value;
		
		double ratioW = maxWidth/(double)w;
		double ratioH = maxHeight/(double)h;
		double ratio = ratioW > ratioH ? ratioH : ratioW;
		int newWidth = new Double(w * ratio).intValue();
		int newHeight = new Double(h * ratio).intValue();
		
		BufferedImage resized = new BufferedImage(newWidth, newHeight, value.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(value, 0, 0, newWidth, newHeight, 0, 0, w, h, null);
		g.dispose();
		return resized;
	}
	
	@Override
	public AttributeValueEditor getEditorComponent(String initialValue, String label)
	{
		return new ImageValueEditor(initialValue, label);
	}
	
	private class ImageValueEditor implements AttributeValueEditor
	{
		private BufferedImageWithExt value;
		private String label;
		private Image field;
		private Upload upload;
		private ProgressBar progressIndicator;
		private CheckBox scale;
		private Label error;
		private boolean required;
		
		public ImageValueEditor(String valueRaw, String label)
		{
			this.value = valueRaw == null ? null : syntax.convertFromString(valueRaw);
			this.label = label;
		}

		@Override
		public ComponentsContainer getEditor(AttributeEditContext context)
		{
			error = new Label();
			error.setStyleName(Styles.error.toString());
			error.setVisible(false);
			
			required = context.isRequired();
			
			Label errorImage = getErrorImage();
			errorImage.setVisible(false);
			
			field = new Image();
			if (value != null)
			{
				try
				{
					BufferedImage scalledPreview = scaleIfNeeded(value.getImage(), PREVIEW_WIDTH, PREVIEW_HEIGHT);
					SimpleImageSource source = new SimpleImageSource(scalledPreview, syntax, value.getType());
					field.setSource(source.getResource());
					errorImage.setVisible(false);
					field.setVisible(true);
				} catch (Exception e)
				{
					log.warn("Problem getting value's image as resource for editing: " + e, e);
					errorImage.setVisible(true);
					field.setVisible(false);
				}
			}
			field.addClickListener(new MouseEvents.ClickListener()
			{
				@Override
				public void click(ClickEvent event)
				{
					if (value != null)
						new ShowImageDialog(syntax, value).show();
				}
			});
			field.setDescription(msg.getMessage("JpegAttributeHandler.clickToEnlarge"));
			
			upload = new Upload();
			progressIndicator = new ProgressBar(0);
			progressIndicator.setVisible(false);
			
			ImageUploader uploader = new ImageUploader(field, syntax, progressIndicator);
			uploader.register();
			upload.setCaption(label);

			upload.setAcceptMimeTypes(
					BufferedImageWithExt.ImageType.getSupportedMimeTypes(","));

			scale = new CheckBox(msg.getMessage("JpegAttributeHandler.scaleIfNeeded"));
			scale.setValue(true);
			return new ComponentsContainer(field, errorImage, error, upload, progressIndicator, scale, 
					getHints(syntax));
		}

		@Override
		public String getCurrentValue() throws IllegalAttributeValueException
		{
			if (value == null && !required)
				return null;
			if (value == null)
			{
				error.setValue(msg.getMessage("JpegAttributeHandler.noImage"));
				error.setVisible(true);
				field.setVisible(false);
				throw new IllegalAttributeValueException(msg.getMessage("JpegAttributeHandler.noImage"));
			}
			try
			{
				syntax.validate(value);
			} catch (IllegalAttributeValueException e)
			{
				error.setValue(e.getMessage());
				error.setVisible(true);
				field.setVisible(false);
				throw e;
			}
			
			error.setVisible(false);
			field.setVisible(true);
			return syntax.convertToString(value);
		}
		
		private class ImageUploader extends AbstractUploadReceiver
		{
			private Image image;
			private LimitedOuputStream fos;
			private ImageAttributeSyntax syntax;
			private BufferedImageWithExt.ImageType type;
			
			public ImageUploader(Image image, ImageAttributeSyntax syntax, ProgressBar progress)
			{
				super(upload, progress);
				this.image = image;
				this.syntax = syntax;
			}

			@Override
			public OutputStream receiveUpload(String filename, String mimeType) 
			{
				int length = syntax.getMaxSize();
				fos = new LimitedOuputStream(length, 
						new ByteArrayOutputStream(length > 102400 ? 102400 : length)); // FIXME - incorrect size definition?
				type = BufferedImageWithExt.ImageType.fromMimeType(mimeType); // FIXME - exception is breaking flow
				return fos;
			}

			// TODO - overide upload Started to verify type.


			@Override
			public void uploadSucceeded(SucceededEvent event) 
			{
				super.uploadSucceeded(event);
				
				if (fos.isOverflow())
				{
					NotificationPopup.showError(
							msg.getMessage("JpegAttributeHandler.uploadFailed"),
							msg.getMessage("JpegAttributeHandler.imageSizeTooBig"));
					fos = null;
					return;
				}
				try
				{
					image.setVisible(true);
					value = new BufferedImageWithExt(
							syntax.deserialize(((ByteArrayOutputStream)
							fos.getWrappedStream()).toByteArray()),
									type);
					if (scale.getValue())
						value.setImage(scaleIfNeeded(value.getImage(), syntax.getMaxWidth(),
								syntax.getMaxHeight()));
					BufferedImage scalledPreview = scaleIfNeeded(value.getImage(),
							PREVIEW_WIDTH, PREVIEW_HEIGHT);
					image.setSource(new SimpleImageSource(scalledPreview, syntax, value.getType()).
							getResource());
				} catch (Exception e)
				{
					NotificationPopup.showError(msg.getMessage("JpegAttributeHandler.uploadInvalid"),
							"");
					fos = null;
				}
			}
		}

		@Override
		public void setLabel(String label)
		{
			upload.setCaption(label);
		};		
	}
	
	private Component getHints(ImageAttributeSyntax syntax)
	{
		Label ret = new Label(msg.getMessage("JpegAttributeHandler.maxSize", syntax.getMaxSize())
				+ "  " + 
				msg.getMessage("JpegAttributeHandler.maxDim", syntax.getMaxWidth(),
						syntax.getMaxHeight()));
		ret.addStyleName(Styles.vLabelSmall.toString());
		return ret;
	}
	
	public static class SimpleImageSource implements StreamSource
	{
		private final byte[] isData;
		private final BufferedImageWithExt.ImageType type;
		
		public SimpleImageSource(BufferedImage value, 
				ImageAttributeSyntax syntax, BufferedImageWithExt.ImageType type)
		{
			this.isData = syntax.serialize(value, type);
			this.type = type;
		}
		
		@Override
		public InputStream getStream()
		{
			return new ByteArrayInputStream(isData);
		}
		
		public Resource getResource()
		{
			return new StreamResource(this, "imgattribute-"+r.nextLong()+r.nextLong()
					+"." + type.toExt());
		}
	}

	@Override
	public Component getSyntaxViewer()
	{
		return new CompactFormLayout(getHints(syntax));
	}

	
	private static class ImageSyntaxEditor implements AttributeSyntaxEditor<BufferedImageWithExt>
	{
		private ImageAttributeSyntax initial;
		private IntegerBoundEditor maxHeight, maxSize;
		private IntegerBoundEditor maxWidth;
		private UnityMessageSource msg;
		private Binder<ImageSyntaxBindingValue> binder;

		public ImageSyntaxEditor(ImageAttributeSyntax initial, UnityMessageSource msg)
		{
			this.initial = initial;
			this.msg = msg;
		}

		@Override
		public Component getEditor()
		{
			FormLayout fl = new CompactFormLayout();
			maxWidth = new IntegerBoundEditor(msg,
					msg.getMessage("JpegAttributeHandler.maxWidthUnlimited"),
					msg.getMessage("JpegAttributeHandler.maxWidthE"),
					Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
			maxHeight = new IntegerBoundEditor(msg,
					msg.getMessage("JpegAttributeHandler.maxHeightUnlimited"),
					msg.getMessage("JpegAttributeHandler.maxHeightE"),
					Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
			maxSize = new IntegerBoundEditor(msg,
					msg.getMessage("JpegAttributeHandler.maxSizeUnlimited"),
					msg.getMessage("JpegAttributeHandler.maxSizeE"),
					Integer.MAX_VALUE, 100, Integer.MAX_VALUE);

			binder = new Binder<>(ImageSyntaxBindingValue.class);
			maxWidth.configureBinding(binder, "maxWidth");
			maxHeight.configureBinding(binder, "maxHeight");
			maxSize.configureBinding(binder, "maxSize");

			fl.addComponents(maxWidth, maxHeight, maxSize);

			ImageSyntaxBindingValue value = new ImageSyntaxBindingValue();
			if (initial != null)
			{
				value.setMaxWidth(initial.getMaxWidth());
				value.setMaxHeight(initial.getMaxHeight());
				value.setMaxSize(initial.getMaxSize());
			} else
			{
				value.setMaxWidth(200);
				value.setMaxHeight(200);
				value.setMaxSize(1024000);
			}
			binder.setBean(value);
			return fl;
		}

		@Override
		public AttributeValueSyntax<BufferedImageWithExt> getCurrentValue()
				throws IllegalAttributeTypeException
		{

			try
			{
				if (!binder.isValid())
				{
					binder.validate();
					throw new IllegalAttributeTypeException("");
				}

				ImageSyntaxBindingValue value = binder.getBean();
				ImageAttributeSyntax ret = new ImageAttributeSyntax();
				ret.setMaxHeight(value.getMaxHeight());
				ret.setMaxWidth(value.getMaxWidth());
				ret.setMaxSize(value.getMaxSize());
				return ret;
			} catch (Exception e)
			{
				throw new IllegalAttributeTypeException(e.getMessage(), e);
			}

		}

		public class ImageSyntaxBindingValue
		{
			private Integer maxSize;
			private Integer maxWidth;
			private Integer maxHeight;
			
			public ImageSyntaxBindingValue()
			{

			}

			public Integer getMaxSize()
			{
				return maxSize;
			}

			public void setMaxSize(Integer maxSize)
			{
				this.maxSize = maxSize;
			}

			public Integer getMaxWidth()
			{
				return maxWidth;
			}

			public void setMaxWidth(Integer maxWidth)
			{
				this.maxWidth = maxWidth;
			}

			public Integer getMaxHeight()
			{
				return maxHeight;
			}

			public void setMaxHeight(Integer maxHeight)
			{
				this.maxHeight = maxHeight;
			}
		}
	}
	
	private class ShowImageDialog extends AbstractDialog
	{
		private ImageAttributeSyntax syntax;
		private BufferedImageWithExt image;
		
		public ShowImageDialog(ImageAttributeSyntax syntax, BufferedImageWithExt image)
		{
			super(ImageAttributeHandler.this.msg,
					ImageAttributeHandler.this.msg.getMessage("JpegAttributeHandler.image"),
					ImageAttributeHandler.this.msg.getMessage("close"));
			this.syntax = syntax;
			this.image = image;
			setSizeMode(SizeMode.LARGE);
		}

		@Override
		protected Component getContents() throws Exception
		{
			Image imageC = new Image();
			SimpleImageSource source = new SimpleImageSource(image.getImage(), syntax, image.getType());
			imageC.setSource(source.getResource());
			return imageC;
		}

		@Override
		protected void onConfirm()
		{
			close();
		}
	}
	
	
	
	@org.springframework.stereotype.Component
	public static class ImageAttributeHandlerFactory implements WebAttributeHandlerFactory
	{
		private UnityMessageSource msg;

		@Autowired
		public ImageAttributeHandlerFactory(UnityMessageSource msg)
		{
			this.msg = msg;
		}
		
		
		@Override
		public String getSupportedSyntaxId()
		{
			return ImageAttributeSyntax.ID;
		}
		
		@Override
		public AttributeSyntaxEditor<BufferedImageWithExt> getSyntaxEditorComponent(
				AttributeValueSyntax<?> initialValue)
		{
			return new ImageSyntaxEditor((ImageAttributeSyntax) initialValue, msg);
		}
		
		@Override
		public WebAttributeHandler createInstance(AttributeValueSyntax<?> syntax)
		{
			return new ImageAttributeHandler(msg, syntax);
		}
	}
}
