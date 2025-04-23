package org.homio.api.ui.field.selection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** File selection */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldTreeNodeSelection {

  String IMAGE_PATTERN = ".*(jpg|jpeg|png|gif)";
  String LOCAL_FS = "LOCAL_FS";

  // is allowed to edit with keyboard
  boolean rawInput() default true;

  String icon() default "fas fa-folder-open";

  String iconColor() default "";

  /**
   * @return If set - uses only local file system, otherwise uses all possible file systems
   */
  String rootPath() default "";

  boolean allowMultiSelect() default false;

  boolean allowSelectDirs() default false;

  boolean allowSelectFiles() default true;

  String pattern() default ".*";

  /**
   * Specify select file/folder dialog title
   *
   * @return - dialog title
   */
  String dialogTitle() default "";

  /**
   * @return Specify file systems ids. All available if not specified
   */
  String[] fileSystemIds() default {UIFieldTreeNodeSelection.LOCAL_FS};

  /**
   * @return detect if need attach metadata to selected file value###{type: file, fs: fileSystem}
   */
  boolean isAttachMetadata() default true;

  /**
   * @return uses on UI to select prefix
   */
  String prefix() default "file";

  /**
   * @return uses on UI to select prefix as backgroundColor
   */
  String prefixColor() default "#707D31";
}
