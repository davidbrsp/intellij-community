// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.actions

import com.intellij.codeInsight.actions.ReaderModeSettings.Companion.applyReaderMode
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorImpl
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import java.util.*

interface ReaderModeListener : EventListener {
  fun modeChanged(project: Project)
}

interface ReaderModeSettingsListener : ReaderModeListener {
  companion object {
    @Topic.ProjectLevel
    @JvmStatic
    val TOPIC = Topic(ReaderModeListener::class.java, Topic.BroadcastDirection.NONE)
  }

  override fun modeChanged(project: Project) {
    FileEditorManager.getInstance(project).allEditors.forEach {
      if (it !is PsiAwareTextEditorImpl) return
      applyReaderMode(project, it.editor, it.file, true) }

    EditorFactory.getInstance().allEditors.forEach {
      if (it !is EditorImpl) return
      applyReaderMode(project, it, FileDocumentManager.getInstance().getFile(it.document), true)
    }
  }
}