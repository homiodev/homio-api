package org.touchhome.bundle.api.entity.storage;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.touchhome.bundle.api.BundleEntryPoint;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.state.DecimalType;
import org.touchhome.bundle.api.state.RawType;
import org.touchhome.bundle.api.util.TouchHomeUtils;
import org.touchhome.bundle.api.workspace.WorkspaceBlock;
import org.touchhome.bundle.api.workspace.scratch.*;

public abstract class Scratch3BaseFileSystemExtensionBlocks<T extends BundleEntryPoint, E extends BaseEntity & BaseFileSystemEntity>
        extends Scratch3ExtensionBlocks {

    private final Class<E> entityClass;

    private final MenuBlock.ServerMenuBlock fsEntityMenu;
    private final MenuBlock.ServerMenuBlock fileMenu;
    private final MenuBlock.ServerMenuBlock folderMenu;
    private final MenuBlock.StaticMenuBlock<Unit> unitMenu;

    private final Scratch3Block sendFile;
    private final Scratch3Block getFileContent;
    private final Scratch3Block deleteFile;
    private final Scratch3Block getUsedQuota;
    private final Scratch3Block getTotalQuota;

    public Scratch3BaseFileSystemExtensionBlocks(String name, String color, EntityContext entityContext, T bundleEntryPoint, Class<E> entityClass) {
        super(color, entityContext, bundleEntryPoint, "storage");
        setParent("storage");
        this.entityClass = entityClass;

        // menu
        this.fsEntityMenu = MenuBlock.ofServerItems(ENTITY, entityClass);
        this.unitMenu = MenuBlock.ofStatic("UNIT", Unit.class, Unit.B);
        String bundleId = bundleEntryPoint.getBundleId();
        this.fileMenu = MenuBlock.ofServer("FILE", "rest/fs/file").setDependency(this.fsEntityMenu)
                .setUIDelimiter("/");
        this.folderMenu = MenuBlock.ofServer("FOLDER", "rest/fs/folder", "/", "/").setDependency(this.fsEntityMenu)
                .setUIDelimiter("/");

        // blocks
        this.sendFile = ofDrive(Scratch3Block.ofHandler(10, "send_file", BlockType.command,
                name + " upload [VALUE] as [NAME] to [PARENT] of [ENTITY] | Append: [APPEND]", this::sendFileHandle));
        this.sendFile.addArgument(VALUE, ArgumentType.string, "body to upload");
        this.sendFile.addArgument("NAME", "test.txt");
        this.sendFile.addArgument("PARENT", this.folderMenu);
        this.sendFile.addArgument("CONTENT", ArgumentType.string);
        this.sendFile.addArgument("APPEND", ArgumentType.checkbox);

        this.getFileContent = ofDrive(Scratch3Block.ofReporter(20, "get_file_content",
                name + " get [FILE] of [ENTITY]", this::getFieldContent));
        this.getFileContent.addArgument("FILE", this.fileMenu);

        this.getUsedQuota = ofDrive(Scratch3Block.ofReporter(30, "get_used_quota",
                name + " used quota if [ENTITY] | in [UNIT]", this::getUsedQuotaReporter));
        this.getUsedQuota.addArgument("UNIT", this.unitMenu);
        this.getTotalQuota = ofDrive(Scratch3Block.ofReporter(40, "get_total_quota",
                name + " total quota of [ENTITY] | in [UNIT]", this::getTotalQuotaReporter));
        this.getTotalQuota.addArgument("UNIT", this.unitMenu);

        this.deleteFile = ofDrive(Scratch3Block.ofHandler(50, "delete", BlockType.command,
                name + " delete [FILE] of [ENTITY]", this::deleteFileHandle));
        this.deleteFile.addArgument("FILE", this.fileMenu);
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
                getDrive(workspaceBlock).getFileSystem(entityContext).delete(fileId.split("~~~"));
            } catch (Exception ex) {
                workspaceBlock.logErrorAndThrow("Unable to delete file: <{}>. Msg: ", fileId, TouchHomeUtils.getErrorMessage(ex));
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
            byte[] bytes = getDrive(workspaceBlock).getFileSystem(entityContext).download(fileId.split("~~~"), true);
            return new RawType(bytes);
        }
        return null;
    }

    @SneakyThrows
    private void sendFileHandle(WorkspaceBlock workspaceBlock) {
        String fileName = workspaceBlock.getInputStringRequired("NAME", "Send file block requires file name");
        byte[] value = workspaceBlock.getInputByteArray(VALUE);

        String folderId = workspaceBlock.getMenuValue("PARENT", this.folderMenu);
        String[] parentPath = folderId.contains("~~~") ? folderId.split("~~~") : folderId.split("/");
        try {
            VendorFileSystem fileSystem = getDrive(workspaceBlock).getFileSystem(entityContext);
            fileSystem.upload(parentPath, fileName, value, workspaceBlock.getInputBoolean("APPEND"));
            fileSystem.updateCache(true);
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
}
