package plugins.nherve.toolbox.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 * 
 * @author Nicolas HERVE - nherve@ina.fr
 */
public class GUIUtil {
	public static JPanel createLineBoxPanel(Component... componentArray) {
		final JPanel result = new JPanel();

		result.setLayout(new BoxLayout(result, BoxLayout.LINE_AXIS));
		for (Component c : componentArray) {
			result.add(c);
		}
		result.validate();

		return result;
	}

	public static JPanel createPageBoxPanel(Component... componentArray) {
		final JPanel result = new JPanel();

		result.setLayout(new BoxLayout(result, BoxLayout.PAGE_AXIS));
		for (Component c : componentArray) {
			result.add(c);
		}
		result.validate();

		return result;
	}

	public static File fileChooser(Component parent, String title, File defaultDirectory, int type) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setPreferredSize(new Dimension(400, 400));
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(type);
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setDialogTitle(title);

		if (defaultDirectory != null) {
			File fp = new File(defaultDirectory.getAbsolutePath());
			while ((fp != null) && (fp.getAbsolutePath().length() > 0) && (!fp.exists())) {
				fp = fp.getParentFile();
			}
			fileChooser.setCurrentDirectory(fp);
		}

		int returnValue = fileChooser.showDialog(parent, "OK");

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			return file;
		} else {
			return null;
		}
	}

	public static void setFixedHeight(Component c, int w, int h) {
		c.setMinimumSize(new Dimension(0, h));
		c.setMaximumSize(new Dimension(65535, h));
		c.setPreferredSize(new Dimension(w, h));
	}

	public static void setFixedWidth(Component c, int w, int h) {
		c.setMinimumSize(new Dimension(w, 0));
		c.setMaximumSize(new Dimension(w, 65535));
		c.setPreferredSize(new Dimension(w, h));
	}
}
