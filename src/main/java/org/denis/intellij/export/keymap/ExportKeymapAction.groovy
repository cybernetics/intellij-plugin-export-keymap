package org.denis.intellij.export.keymap;


import com.intellij.ide.util.projectWizard.NamePathComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import com.intellij.openapi.ui.DialogWrapper
import groovy.swing.SwingBuilder
import org.denis.intellij.export.keymap.generator.GeneratorFacade
import org.denis.intellij.export.keymap.model.ActionsProfile
import org.denis.intellij.export.keymap.model.Settings

import javax.swing.JComponent
import com.intellij.openapi.keymap.Keymap

/**
 * @author Denis Zhdanov
 * @since 6/5/12 3:47 PM
 */
class ExportKeymapAction extends AnAction {

  public ExportKeymapAction() {
    getTemplatePresentation().setText(Bundle.message("action.export.keymap.name"));
    getTemplatePresentation().setDescription(Bundle.message("action.export.keymap.name"));
  }

  @Override
  void update(AnActionEvent e) {
    def project = PlatformDataKeys.PROJECT.getData(e.dataContext)
    e.presentation.enabled = project != null
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    def settings = Settings.instance
    def keymaps = KeymapManagerEx.instanceEx.allKeymaps
    keymaps = keymaps.sort { a, b ->
      if (a.name == settings.keymapName) return -1
      if (b.name == settings.keymapName) return 1
      a.name <=> b.name
    }
    
    def pathText = Bundle.message('label.path')
    def pathControl = new NamePathComponent('', '', pathText, '', false, false)
    pathControl.nameComponentVisible = false
    pathControl.pathPanel.remove(pathControl.pathLabel)
    def keyMapComboBox
    def content = new SwingBuilder().panel() {
      gridBagLayout()
      emptyBorder([15, 15, 10, 15], parent: true)
      def labelConstraints = gbc(anchor: WEST, insets: [0, 0, 5, 5])
      def controlConstraints = gbc(gridwidth: REMAINDER, fill: HORIZONTAL, insets: [0, 4, 5, 0])
      
      label(text: Bundle.message("label.keymap") + ":", constraints: labelConstraints)
      keyMapComboBox = comboBox(items: keymaps.collect { it.presentableName }, constraints: controlConstraints)

      label("$pathText:", constraints: labelConstraints)
      controlConstraints.insets.left = 0
      widget(pathControl, constraints: controlConstraints)
    }
    
    def dialog = new DialogWrapper(PlatformDataKeys.PROJECT.getData(e.dataContext)) {
      
      {
        init()
      }
      
      @Override protected JComponent createCenterPanel() { content }
    }
    dialog.title = Bundle.message('action.export.keymap.name')
    
    dialog.show()
    if (!dialog.OK) {
      return
    }
    
    Keymap keymap = keymaps[keyMapComboBox.selectedIndex]
    settings.keymapName = keymap.name

    new GeneratorFacade().generate(keymap, ActionsProfile.DEFAULT, '/home/denis/Downloads/output.pdf')
  }
}
