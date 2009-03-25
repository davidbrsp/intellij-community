
package com.intellij.testFramework;

import com.intellij.openapi.application.ex.PathManagerEx;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.jsp.JspFile;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public abstract class ParsingTestCase extends LightIdeaTestCase {
  protected String myFileExt;
  @NonNls private final String myFullDataPath;
  protected PsiFile myFile;

  public ParsingTestCase(@NonNls String dataPath, String fileExt) {
    myFullDataPath = getTestDataPath() + "/psi/" + dataPath;
    myFileExt = fileExt;
  }

  protected String getTestDataPath() {
    return PathManagerEx.getTestDataPath();
  }

  protected boolean includeRanges() {
    return false;
  }

  protected void doTest(boolean checkResult) throws Exception{
    String name = getTestName(false);
    String text = loadFile(name + "." + myFileExt);
    myFile = createFile(name + "." + myFileExt, text);
    myFile.accept(new PsiRecursiveElementVisitor(){});
    assertEquals(text, myFile.getText());
    if (checkResult){
      checkResult(name + ".txt", myFile);
    }
    else{
      toParseTreeText(myFile, includeRanges());
    }
    if(myFile instanceof JspFile) ((MultiplePsiFilesPerDocumentFileViewProvider)((JspFile)myFile).getViewProvider()).checkAllTreesEqual();
  }

  protected void checkResult(@NonNls String targetDataName, final PsiFile file) throws Exception {
    doCheckResult(myFullDataPath, file, targetDataName, includeRanges());
  }

  public static void doCheckResult(String myFullDataPath, PsiFile file, String targetDataName, boolean printRanges) throws Exception {
    final PsiElement[] psiRoots = file.getPsiRoots();
    if(psiRoots.length > 1){
      for (int i = 0; i < psiRoots.length; i++) {
        final PsiElement psiRoot = psiRoots[i];
        doCheckResult(myFullDataPath, targetDataName + "." + i, toParseTreeText(psiRoot, printRanges).trim());
      }
    }
    else{
      doCheckResult(myFullDataPath, targetDataName, toParseTreeText(file, printRanges).trim());
    }
  }

  protected void checkResult(@NonNls String targetDataName, final String text) throws Exception {
    doCheckResult(myFullDataPath, targetDataName, text);
  }

  private static void doCheckResult(String myFullDataPath, String targetDataName, String text) throws Exception {
    try {
      text = text.trim();
      String expectedText = doLoadFile(myFullDataPath, targetDataName);
      assertEquals(expectedText, text);
    }
    catch(FileNotFoundException e){
      String fullName = myFullDataPath + File.separatorChar + targetDataName;
      FileWriter writer = new FileWriter(fullName);
      try {
        writer.write(text);
      }
      finally {
        writer.close();
      }
      fail("No output text found. File " + fullName + " created.");
    }
  }

  protected static String toParseTreeText(final PsiElement file, boolean printRanges) {
    return DebugUtil.psiToString(file, false, printRanges);
  }

  protected String loadFile(@NonNls String name) throws Exception {
    return doLoadFile(myFullDataPath, name);
  }

  private static String doLoadFile(String myFullDataPath, String name) throws IOException {
    String fullName = myFullDataPath + File.separatorChar + name;
    String text = new String(FileUtil.loadFileText(new File(fullName))).trim();
    text = StringUtil.convertLineSeparators(text);
    return text;
  }
}
