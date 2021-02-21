package org.touchhome.bundle.api.fs;

import lombok.SneakyThrows;
import org.springframework.util.MimeTypeUtils;
import org.touchhome.bundle.api.BundleEntryPoint;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.state.RawType;
import org.touchhome.bundle.api.state.State;
import org.touchhome.bundle.api.workspace.WorkspaceBlock;
import org.touchhome.bundle.api.workspace.scratch.*;

import java.nio.charset.Charset;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public abstract class Scratch3BaseFileSystemExtensionBlocks<T extends BundleEntryPoint, E extends BaseFileSystemEntity>
        extends Scratch3ExtensionBlocks {

    private final Class<E> entityClass;

    private final MenuBlock.ServerMenuBlock fsEntityMenu;
    private final MenuBlock.ServerMenuBlock fileMenu;
    private final MenuBlock.ServerMenuBlock folderMenu;

    private final Scratch3Block sendFile;
    private final Scratch3Block getFileContent;
    private final Scratch3Block deleteFile;
    private final Scratch3Block getUsedQuota;
    private final Scratch3Block getTotalQuota;

    public Scratch3BaseFileSystemExtensionBlocks(String name, String color, EntityContext entityContext, T bundleEntryPoint, Class<E> entityClass) {
        super(color, entityContext, bundleEntryPoint);
        setParent("cloudstorage");
        this.entityClass = entityClass;

        // menu
        this.fsEntityMenu = MenuBlock.ofServerItems(ENTITY, entityClass);
        String bundleId = bundleEntryPoint.getBundleId();
        this.fileMenu = MenuBlock.ofServer("FILE", "rest/fs/file").setDependency(this.fsEntityMenu)
                .setUIDelimiter("/");
        this.folderMenu = MenuBlock.ofServer("FOLDER", "rest/fs/folder", "/", "/").setDependency(this.fsEntityMenu)
                .setUIDelimiter("/");

        // blocks
        this.sendFile = ofDrive(Scratch3Block.ofHandler(10, "send_file", BlockType.command,
                name + " upload [VALUE] as [NAME] to [PARENT] of [ENTITY]", this::sendFileHandle));
        this.sendFile.addArgument(VALUE, ArgumentType.string, "body to upload");
        this.sendFile.addArgument("NAME", "test.txt");
        this.sendFile.addArgument("PARENT", this.folderMenu);
        this.sendFile.addArgument("CONTENT", ArgumentType.string);

        this.getFileContent = ofDrive(Scratch3Block.ofEvaluate(20, "get_file_content", BlockType.reporter,
                name + " get [FILE] of [ENTITY]", this::getFieldContent));
        this.getFileContent.addArgument("FILE", this.fileMenu);

        this.getUsedQuota = ofDrive(Scratch3Block.ofEvaluate(30, "get_used_quota", BlockType.reporter,
                name + " used quota if [ENTITY]", this::getUsedQuotaReporter));
        this.getTotalQuota = ofDrive(Scratch3Block.ofEvaluate(40, "get_total_quota", BlockType.reporter,
                name + " total quota of [ENTITY]", this::getTotalQuotaReporter));

        this.deleteFile = ofDrive(Scratch3Block.ofHandler(50, "delete", BlockType.command,
                name + " delete [FILE] of [ENTITY]", this::deleteFileHandle));
        this.deleteFile.addArgument("FILE", this.fileMenu);
    }

    public void init() {
        this.fsEntityMenu.setDefault(entityContext.findAny(entityClass));
        super.init();
    }

    @SneakyThrows
    private Object getTotalQuotaReporter(WorkspaceBlock workspaceBlock) {
        return getDrive(workspaceBlock).getFileSystem(entityContext).getTotalSpace();
    }

    private Object getUsedQuotaReporter(WorkspaceBlock workspaceBlock) {
        return getDrive(workspaceBlock).getFileSystem(entityContext).getUsedSpace();
    }

    private void deleteFileHandle(WorkspaceBlock workspaceBlock) {
        String fileId = workspaceBlock.getMenuValue("FILE", this.fileMenu);
        if (!"-".equals(fileId)) {
            try {
                getDrive(workspaceBlock).getFileSystem(entityContext).delete(fileId.split("~~~"));
            } catch (Exception ex) {
                workspaceBlock.logErrorAndThrow("Unable to delete file: <{}>", fileId);
            }
        } else {
            workspaceBlock.logErrorAndThrow("Delete file block requires file name");
        }
    }

    private E getDrive(WorkspaceBlock workspaceBlock) {
        return workspaceBlock.getMenuValueEntity(ENTITY, this.fsEntityMenu);
    }

    private RawType getFieldContent(WorkspaceBlock workspaceBlock) throws Exception {
        String fileId = workspaceBlock.getMenuValue("FILE", this.fileMenu);
        if (!"-".equals(fileId)) {
            byte[] bytes = getDrive(workspaceBlock).getFileSystem(entityContext).download(fileId.split("~~~"), true);
            return new RawType(bytes, MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE);
        }
        return null;
    }

    @SneakyThrows
    private void sendFileHandle(WorkspaceBlock workspaceBlock) {
        String fileName = workspaceBlock.getInputString("NAME");
        Object content = workspaceBlock.getInput(VALUE, true);
        byte[] value;
        if (content instanceof State) {
            value = ((State) content).byteArrayValue();
        } else if (content instanceof byte[]) {
            value = (byte[]) content;
        } else {
            value = content.toString().getBytes(Charset.defaultCharset());
        }

        String folderId = workspaceBlock.getMenuValue("PARENT", this.folderMenu);
        if (isNotEmpty(fileName)) {
            String[] parentPath = folderId.contains("~~~") ? folderId.split("~~~") : folderId.split("/");
            try {
                getDrive(workspaceBlock).getFileSystem(entityContext).upload(parentPath, fileName, value, null);
            } catch (Exception ex) {
                workspaceBlock.logError("Unable to store file: <{}>. Msg: <{}>", fileName, ex.getMessage());
            }
        } else {
            workspaceBlock.logErrorAndThrow("Send file block requires file name");
        }
    }

    private Scratch3Block ofDrive(Scratch3Block scratch3Block) {
        scratch3Block.addArgument(ENTITY, this.fsEntityMenu);
        return scratch3Block;
    }
}
