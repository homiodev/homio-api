package org.touchhome.bundle.api.entity.storage;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MimeTypeUtils;
import org.touchhome.bundle.api.BundleEntrypoint;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.state.DecimalType;
import org.touchhome.bundle.api.state.RawType;
import org.touchhome.bundle.api.workspace.WorkspaceBlock;
import org.touchhome.bundle.api.workspace.scratch.ArgumentType;
import org.touchhome.bundle.api.workspace.scratch.MenuBlock;
import org.touchhome.bundle.api.workspace.scratch.Scratch3ExtensionBlocks;
import org.touchhome.common.fs.FileSystemProvider;
import org.touchhome.common.fs.TreeNode;
import org.touchhome.common.util.CommonUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class Scratch3BaseFileSystemExtensionBlocks<T extends BundleEntrypoint,
        E extends BaseEntity & BaseFileSystemEntity>
        extends Scratch3ExtensionBlocks {

    private final Class<E> entityClass;

    private final MenuBlock.StaticMenuBlock<UploadOption> uploadOptionsMenu;
    private final MenuBlock.ServerMenuBlock fsEntityMenu;
    private final MenuBlock.ServerMenuBlock fileMenu;
    private final MenuBlock.ServerMenuBlock folderMenu;
    private final MenuBlock.StaticMenuBlock<Unit> unitMenu;
    private final MenuBlock.StaticMenuBlock<CountNodeEnum> countMenu;

    public Scratch3BaseFileSystemExtensionBlocks(String color, EntityContext entityContext, T bundleEntryPoint,
                                                 Class<E> entityClass) {
        super(color, entityContext, bundleEntryPoint, "storage");
        setParent("storage");
        this.entityClass = entityClass;

        // menu
        this.fsEntityMenu = menuServerItems(ENTITY, entityClass, "FileSystem");
        this.uploadOptionsMenu =
                menuStatic("uploadOptionsMenu", UploadOption.class, UploadOption.Overwrite).setMultiSelect(" | ");
        this.unitMenu = menuStatic("UNIT", Unit.class, Unit.B);
        this.countMenu = menuStatic("COUNT", CountNodeEnum.class, CountNodeEnum.All);
        this.fileMenu = menuServerFiles(this.fsEntityMenu, null);
        this.folderMenu = menuServerFolders(this.fsEntityMenu, null);

        // blocks
        blockCommand(15, "modify_file",
                "Update [VALUE] as [NAME] to [PARENT] of [ENTITY] | Options: [OPTIONS]", this::sendFileHandle,
                block -> {
                    block.addArgument(ENTITY, this.fsEntityMenu);
                    block.addArgument(VALUE, ArgumentType.string, "body");
                    block.addArgument("NAME", "test.txt");
                    block.addArgument("PARENT", this.folderMenu);
                    block.addArgument("CONTENT", ArgumentType.string);
                    block.addArgument("OPTIONS", this.uploadOptionsMenu);
                });


        blockReporter(20, "get_file_content", "Get [FILE] of [ENTITY]", this::getFieldContent,
                block -> {
                    block.addArgument(ENTITY, this.fsEntityMenu);
                    block.addArgument("FILE", this.fileMenu);
                });

        blockReporter(30, "get_count", "Count of [VALUE] in [PARENT] [ENTITY]", this::getCountOfNodesReporter,
                block -> {
                    block.addArgument(ENTITY, this.fsEntityMenu);
                    block.addArgument("PARENT", this.folderMenu);
                    block.addArgument(VALUE, this.countMenu);
                });

        blockReporter(35, "get_used_quota", "Used quota if [ENTITY] | in [UNIT]", this::getUsedQuotaReporter,
                block -> {
                    block.addArgument(ENTITY, this.fsEntityMenu);
                    block.addArgument("UNIT", this.unitMenu);
                });

        blockReporter(40, "get_total_quota", "Total quota of [ENTITY] | in [UNIT]", this::getTotalQuotaReporter,
                block -> {
                    block.addArgument(ENTITY, this.fsEntityMenu);
                    block.addArgument("UNIT", this.unitMenu);
                });

        blockCommand(50, "delete", "Delete [FILE] of [ENTITY]", this::deleteFileHandle,
                block -> {
                    block.addArgument(ENTITY, this.fsEntityMenu);
                    block.addArgument("FILE", this.fileMenu);
                });
    }

    public static byte[] addAll(final byte[] array1, byte[] array2) {
        byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    private DecimalType getCountOfNodesReporter(WorkspaceBlock workspaceBlock) {
        CountNodeEnum countNodeEnum = workspaceBlock.getMenuValue(VALUE, this.countMenu);
        FileSystemProvider fileSystem = getDrive(workspaceBlock).getFileSystem(entityContext);
        String folderId = workspaceBlock.getMenuValue("PARENT", this.folderMenu);
        Set<TreeNode> children = fileSystem.getChildren(folderId);
        switch (countNodeEnum) {
            case Files:
                return new DecimalType(children.stream().filter(c -> !c.getAttributes().isDir()).count());
            case Folders:
                return new DecimalType(children.stream().filter(c -> c.getAttributes().isDir()).count());
            case All:
                return new DecimalType(children.size());
            case FilesWithChildren:
                AtomicInteger filesCounter = new AtomicInteger(0);
                Consumer<TreeNode> filesFilter = treeNode -> {
                    if (!treeNode.getAttributes().isDir()) {
                        filesCounter.incrementAndGet();
                    }
                };
                for (TreeNode treeNode : fileSystem.getChildrenRecursively(folderId)) {
                    treeNode.visitTree(filesFilter);
                }
                return new DecimalType(filesCounter.get());
            case AllWithChildren:
                AtomicInteger allNodesCounter = new AtomicInteger(0);
                Consumer<TreeNode> allNodesFilter = treeNode -> allNodesCounter.incrementAndGet();
                for (TreeNode treeNode : fileSystem.getChildrenRecursively(folderId)) {
                    treeNode.visitTree(allNodesFilter);
                }
                return new DecimalType(allNodesCounter.get());
        }
        throw new RuntimeException("Unable to handle unknown count enum type: " + countNodeEnum);
    }

    public void init() {
        this.fsEntityMenu.setDefault(entityContext.findAny(entityClass));
        super.init();
    }

    @SneakyThrows
    private DecimalType getTotalQuotaReporter(WorkspaceBlock workspaceBlock) {
        double unit = workspaceBlock.getMenuValue("UNIT", this.unitMenu).divider;
        return new DecimalType(getDrive(workspaceBlock).getFileSystem(entityContext).getTotalSpace() / unit);
    }

    private DecimalType getUsedQuotaReporter(WorkspaceBlock workspaceBlock) {
        double unit = workspaceBlock.getMenuValue("UNIT", this.unitMenu).divider;
        return new DecimalType(getDrive(workspaceBlock).getFileSystem(entityContext).getUsedSpace() / unit);
    }

    private void deleteFileHandle(WorkspaceBlock workspaceBlock) {
        String fileId = workspaceBlock.getMenuValue("FILE", this.fileMenu);
        if (!"-".equals(fileId)) {
            try {
                getDrive(workspaceBlock).getFileSystem(entityContext).delete(Collections.singleton(fileId));
            } catch (Exception ex) {
                workspaceBlock.logErrorAndThrow("Unable to delete file: <{}>. Msg: ", fileId, CommonUtils.getErrorMessage(ex));
            }
        } else {
            workspaceBlock.logErrorAndThrow("Delete file block requires file name");
        }
    }

    private E getDrive(WorkspaceBlock workspaceBlock) {
        return workspaceBlock.getMenuValueEntityRequired(ENTITY, this.fsEntityMenu);
    }

    private RawType getFieldContent(WorkspaceBlock workspaceBlock) throws Exception {
        String fileId = workspaceBlock.getMenuValue("FILE", this.fileMenu);
        if (!"-".equals(fileId)) {
            String[] path = fileId.split("~~~");
            String id = path[path.length - 1];
            FileSystemProvider fileSystem = getDrive(workspaceBlock).getFileSystem(entityContext);
            TreeNode treeNode = fileSystem.toTreeNode(id);
            byte[] content = IOUtils.toByteArray(treeNode.getInputStream());

            return new RawType(content,
                    StringUtils.defaultIfEmpty(treeNode.getAttributes().getContentType(), MimeTypeUtils.TEXT_PLAIN_VALUE),
                    treeNode.getName());
        }
        return null;
    }

    @SneakyThrows
    private void sendFileHandle(WorkspaceBlock workspaceBlock) {
        String fileName = workspaceBlock.getInputStringRequired("NAME", "Send file block requires file name");
        byte[] value = workspaceBlock.getInputByteArray(VALUE);

        String folderId = workspaceBlock.getMenuValue("PARENT", this.folderMenu);
        try {
            FileSystemProvider fileSystem = getDrive(workspaceBlock).getFileSystem(entityContext);
            List<UploadOption> uploadOptions =
                    workspaceBlock.getMenuValues("OPTIONS", this.uploadOptionsMenu, UploadOption.class, "~~~");

            FileSystemProvider.UploadOption uploadOption = null;
            if (!uploadOptions.contains(UploadOption.Overwrite)) {
                if (uploadOptions.contains(UploadOption.PrependNewLine)) {
                    value = addAll("\n".getBytes(), value);
                }
                if (uploadOptions.contains(UploadOption.AppendNewLine)) {
                    value = addAll(value, "\n".getBytes());
                }

                uploadOption = uploadOptions.contains(UploadOption.Append) ? FileSystemProvider.UploadOption.Append :
                        FileSystemProvider.UploadOption.Replace;
            }

            fileSystem.copy(TreeNode.of(fileName, value), folderId, uploadOption);
        } catch (Exception ex) {
            workspaceBlock.logError("Unable to store file: <{}>. Msg: <{}>", fileName, ex.getMessage());
        }
    }

    @RequiredArgsConstructor
    private enum Unit {
        B(1), KB(1024), MP(1024 * 1024), GB(1024 * 1024 * 1024);
        private final double divider;
    }

    private enum UploadOption {
        Overwrite, Append, PrependNewLine, AppendNewLine
    }

    private enum CountNodeEnum {
        Files, Folders, All, FilesWithChildren, AllWithChildren
    }
}
