package org.touchhome.bundle.api.entity.storage;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MimeTypeUtils;
import org.touchhome.bundle.api.BundleEntryPoint;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.state.DecimalType;
import org.touchhome.bundle.api.state.RawType;
import org.touchhome.bundle.api.workspace.WorkspaceBlock;
import org.touchhome.bundle.api.workspace.scratch.*;
import org.touchhome.common.fs.FileObject;
import org.touchhome.common.fs.FileSystemProvider;
import org.touchhome.common.util.CommonUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class Scratch3BaseFileSystemExtensionBlocks<T extends BundleEntryPoint,
        E extends BaseEntity & BaseFileSystemEntity>
        extends Scratch3ExtensionBlocks {

    private final Class<E> entityClass;

    private final MenuBlock.StaticMenuBlock<UploadOption> uploadOptionsMenu;
    private final MenuBlock.ServerMenuBlock fsEntityMenu;
    private final MenuBlock.ServerMenuBlock fileMenu;
    private final MenuBlock.ServerMenuBlock folderMenu;
    private final MenuBlock.StaticMenuBlock<Unit> unitMenu;
    private final MenuBlock.StaticMenuBlock<CountNodeEnum> countMenu;

    private final Scratch3Block sendFile;
    private final Scratch3Block modifyFile;

    private final Scratch3Block getFileContent;
    private final Scratch3Block deleteFile;
    private final Scratch3Block getUsedQuota;
    private final Scratch3Block getTotalQuota;
    private final Scratch3Block count;

    public Scratch3BaseFileSystemExtensionBlocks(String name, String color, EntityContext entityContext, T bundleEntryPoint,
                                                 Class<E> entityClass) {
        super(color, entityContext, bundleEntryPoint, "storage");
        setParent("storage");
        this.entityClass = entityClass;

        // menu
        this.fsEntityMenu = MenuBlock.ofServerItems(ENTITY, entityClass, "FileSystem");
        this.uploadOptionsMenu =
                MenuBlock.ofStatic("uploadOptionsMenu", UploadOption.class, UploadOption.Append).setMultiSelect(" | ");
        this.unitMenu = MenuBlock.ofStatic("UNIT", Unit.class, Unit.B);
        this.countMenu = MenuBlock.ofStatic("COUNT", CountNodeEnum.class, CountNodeEnum.All);
        this.fileMenu = MenuBlock.ofServerFiles(this.fsEntityMenu, null);
        this.folderMenu = MenuBlock.ofServerFolders(this.fsEntityMenu, null);

        // blocks
        this.sendFile = ofDrive(Scratch3Block.ofHandler(10, "send_file", BlockType.command,
                "Upload [VALUE] as [NAME] to [PARENT] of [ENTITY]", this::sendFileHandle));
        this.sendFile.addArgument(VALUE, ArgumentType.string, "body");
        this.sendFile.addArgument("NAME", "test.txt");
        this.sendFile.addArgument("PARENT", this.folderMenu);
        this.sendFile.addArgument("CONTENT", ArgumentType.string);

        this.modifyFile = ofDrive(Scratch3Block.ofHandler(15, "modify_file", BlockType.command,
                "Update [VALUE] as [NAME] to [PARENT] of [ENTITY] | Options: [OPTIONS]", this::sendFileHandle));
        this.modifyFile.addArgument(VALUE, ArgumentType.string, "body");
        this.modifyFile.addArgument("NAME", "test.txt");
        this.modifyFile.addArgument("PARENT", this.folderMenu);
        this.modifyFile.addArgument("CONTENT", ArgumentType.string);
        this.modifyFile.addArgument("OPTIONS", this.uploadOptionsMenu);

        this.getFileContent =
                ofDrive(Scratch3Block.ofReporter(20, "get_file_content", "Get [FILE] of [ENTITY]", this::getFieldContent));
        this.getFileContent.addArgument("FILE", this.fileMenu);

        this.count = ofDrive(Scratch3Block.ofReporter(30, "get_count", "Count of [VALUE] in [PARENT] [ENTITY]",
                this::getCountOfNodesReporter));
        this.count.addArgument("PARENT", this.folderMenu);
        this.count.addArgument(VALUE, this.countMenu);

        this.getUsedQuota = ofDrive(Scratch3Block.ofReporter(35, "get_used_quota", "Used quota if [ENTITY] | in [UNIT]",
                this::getUsedQuotaReporter));
        this.getUsedQuota.addArgument("UNIT", this.unitMenu);
        this.getTotalQuota = ofDrive(Scratch3Block.ofReporter(40, "get_total_quota", "Total quota of [ENTITY] | in [UNIT]",
                this::getTotalQuotaReporter));
        this.getTotalQuota.addArgument("UNIT", this.unitMenu);

        this.deleteFile = ofDrive(Scratch3Block.ofHandler(50, "delete", BlockType.command, "Delete [FILE] of [ENTITY]",
                this::deleteFileHandle));
        this.deleteFile.addArgument("FILE", this.fileMenu);
    }

    private DecimalType getCountOfNodesReporter(WorkspaceBlock workspaceBlock) {
        CountNodeEnum countNodeEnum = workspaceBlock.getMenuValue(VALUE, this.countMenu);
        FileSystemProvider fileSystem = getDrive(workspaceBlock).getFileSystem(entityContext);
        String folderId = workspaceBlock.getMenuValue("PARENT", this.folderMenu);
        Set<FileObject> children = fileSystem.getChildren(folderId);
        switch (countNodeEnum) {
            case Files:
                return new DecimalType(children.stream().filter(c -> !c.getAttributes().isDir()).count());
            case Folders:
                return new DecimalType(children.stream().filter(c -> c.getAttributes().isDir()).count());
            case All:
                return new DecimalType(children.size());
            case FilesWithChildren:
                AtomicInteger filesCounter = new AtomicInteger(0);
                Consumer<FileObject> filesFilter = fileObject -> {
                    if (!fileObject.getAttributes().isDir()) {
                        filesCounter.incrementAndGet();
                    }
                };
                for (FileObject fileObject : fileSystem.getChildrenRecursively(folderId)) {
                    fileObject.visitFileObjectTree(filesFilter);
                }
                return new DecimalType(filesCounter.get());
            case AllWithChildren:
                AtomicInteger allNodesCounter = new AtomicInteger(0);
                Consumer<FileObject> allNodesFilter = fileObject -> allNodesCounter.incrementAndGet();
                for (FileObject fileObject : fileSystem.getChildrenRecursively(folderId)) {
                    fileObject.visitFileObjectTree(allNodesFilter);
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
            FileObject fileObject = fileSystem.toFileObject(id);
            byte[] content = IOUtils.toByteArray(fileObject.getInputStream());

            return new RawType(content,
                    StringUtils.defaultIfEmpty(fileObject.getAttributes().getContentType(), MimeTypeUtils.TEXT_PLAIN_VALUE),
                    fileObject.getName());
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

            if (uploadOptions.contains(UploadOption.PrependNewLine)) {
                value = addAll("\n".getBytes(), value);
            }
            if (uploadOptions.contains(UploadOption.AppendNewLine)) {
                value = addAll(value, "\n".getBytes());
            }

            FileSystemProvider.UploadOption uploadOption =
                    uploadOptions.contains(UploadOption.Append) ? FileSystemProvider.UploadOption.Append :
                            FileSystemProvider.UploadOption.Replace;

            fileSystem.copy(FileObject.of(fileName, value), folderId, uploadOption);
        } catch (Exception ex) {
            workspaceBlock.logError("Unable to store file: <{}>. Msg: <{}>", fileName, ex.getMessage());
        }
    }

    private Scratch3Block ofDrive(Scratch3Block scratch3Block) {
        scratch3Block.addArgument(ENTITY, this.fsEntityMenu);
        return scratch3Block;
    }

    @RequiredArgsConstructor
    private enum Unit {
        B(1), KB(1024), MP(1024 * 1024), GB(1024 * 1024 * 1024);
        private final double divider;
    }

    private enum UploadOption {
        Append, PrependNewLine, AppendNewLine
    }

    private enum CountNodeEnum {
        Files, Folders, All, FilesWithChildren, AllWithChildren
    }

    public static byte[] addAll(final byte[] array1, byte[] array2) {
        byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }
}
