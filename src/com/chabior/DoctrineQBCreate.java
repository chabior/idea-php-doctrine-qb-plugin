package com.chabior;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.Statement;
import net.sf.jsqlparser.JSQLParserException;
import org.jetbrains.annotations.Nullable;


public class DoctrineQBCreate extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String txt = Messages.showInputDialog(project, "Your query", "Input Your Query", Messages.getQuestionIcon());
        Method method = getMethod(editor, project);

        try {
            String query = new QueryBuilder().build(txt);

            WriteCommandAction.Simple.runWriteCommandAction(project, () -> {
                final Statement statement = PhpPsiElementFactory.createStatement(project, query);
                final PsiElement groupStatement = method.getLastChild();
                if (groupStatement instanceof GroupStatement) {
                    final PsiElement element = findElementAtCaret(editor, project);
                    if (element != null) {
                        groupStatement.addBefore(statement, element);
                    }
                }
            });
        } catch (JSQLParserException ignored) {
            Notifications.Bus.notify(new Notification(
                    "Doctrine QB",
                    "Parse error",
                    ignored.getMessage(),
                    NotificationType.ERROR
            ));
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Project project = e.getProject();
        Method method = this.getMethod(editor, project);
        e.getPresentation().setEnabled(method != null);
    }

    @Nullable
    private Method getMethod(Editor editor, Project project) {
        PsiElement psiElement = findElementAtCaret(editor, project);
        if (psiElement == null) {
            return null;
        }

        PhpClass phpClass = PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);
        if(phpClass == null) {
            return null;
        }

        Method method = PsiTreeUtil.getParentOfType(psiElement, Method.class);
        if(method == null) {
            return null;
        }

        return method;
    }

    @Nullable
    private PsiElement findElementAtCaret(Editor editor, Project project) {
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);

        if(file == null) {
            return null;
        }

        int offset = editor.getCaretModel().getOffset();
        if(offset <= 0) {
            return null;
        }

        PsiElement psiElement = file.findElementAt(offset);
        if(psiElement == null) {
            return null;
        }

        return psiElement;
    }
}
