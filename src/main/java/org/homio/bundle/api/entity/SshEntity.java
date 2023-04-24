package org.homio.bundle.api.entity;

import static com.sshtools.common.publickey.SshKeyPairGenerator.ECDSA;
import static com.sshtools.common.publickey.SshKeyPairGenerator.ED25519;
import static com.sshtools.common.publickey.SshKeyPairGenerator.SSH2_RSA;
import static org.apache.logging.log4j.util.Strings.trimToNull;

import com.sshtools.common.publickey.SshKeyPairGenerator;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.publickey.SshPrivateKeyFile;
import com.sshtools.common.publickey.SshPrivateKeyFileFactory;
import com.sshtools.common.ssh.components.SshKeyPair;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.entity.types.CommunicationEntity;
import org.homio.bundle.api.model.ActionResponseModel;
import org.homio.bundle.api.model.FileContentType;
import org.homio.bundle.api.model.FileModel;
import org.homio.bundle.api.service.SshProviderService;
import org.homio.bundle.api.ui.UISidebarChildren;
import org.homio.bundle.api.ui.field.UIField;
import org.homio.bundle.api.ui.field.UIFieldGroup;
import org.homio.bundle.api.ui.field.UIFieldType;
import org.homio.bundle.api.ui.field.action.UIActionInput;
import org.homio.bundle.api.ui.field.action.UIActionInput.Type;
import org.homio.bundle.api.ui.field.action.UIContextMenuAction;
import org.homio.bundle.api.ui.field.selection.UIFieldBeanSelection;
import org.homio.bundle.api.util.SecureString;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

@Getter
@Setter
@Entity
@UISidebarChildren(icon = "fas fa-terminal", color = "#0088CC")
public class SshEntity extends CommunicationEntity<SshEntity> implements HasStatusAndMsg<SshEntity> {

    public static final String PREFIX = "ssh_";

    @UIField(order = 1, hideInEdit = true, hideOnEmpty = true, fullWidth = true, bg = "#334842C2", type = UIFieldType.HTML)
    public String getDescription() {
        return getJsonData("description");
    }

    public void setDescription(String value) {
        setJsonData("description", value);
    }

    @UIField(order = 10, required = true, inlineEditWhenEmpty = true)
    @UIFieldBeanSelection(value = SshProviderService.class)
    public String getSshProviderService() {
        return getJsonData("provider");
    }

    public void setSshProviderService(String value) {
        setJsonData("provider", value);
    }

    @UIField(order = 55)
    public String getHost() {
        return getJsonData("host");
    }

    public void setHost(String value) {
        setJsonData("host", value);
    }

    @UIField(order = 60)
    public int getPort() {
        return getJsonData("port", 22);
    }

    public void setPort(int value) {
        setJsonData("port", value);
    }

    @UIField(order = 1)
    @UIFieldGroup(order = 2, value = "Security", borderColor = "#23ADAB")
    public String getUser() {
        return getJsonData("user", "");
    }

    public void setUser(String value) {
        setJsonData("user", value);
    }

    @UIField(order = 2)
    @UIFieldGroup("Security")
    public SecureString getPassword() {
        return getJsonSecure("pwd");
    }

    public void setPassword(String value) {
        setJsonData("pwd", value);
    }

    @UIField(order = 3, hideInEdit = true)
    @UIFieldGroup("Security")
    public boolean hasPrivateKey() {
        return getJsonData().has("key_pwd");
    }

    @UIField(order = 4, hideInEdit = true, hideOnEmpty = true)
    public String getFingerprint() {
        return getJsonData("fp");
    }

    @UIField(order = 5, hideInEdit = true, hideOnEmpty = true)
    public String getAlgorithm() {
        return getJsonData("alg");
    }

    @UIField(order = 6, hideInEdit = true, hideOnEmpty = true)
    public boolean isPrivateKeyPasswordProtected() {
        return StringUtils.isNotEmpty(getJsonData("key_pwd"));
    }

    @Override
    public String getDefaultName() {
        return "SSH";
    }

    @Override
    public @NotNull String getEntityPrefix() {
        return PREFIX;
    }

    @SneakyThrows
    @UIContextMenuAction(value = "generate_private_key", icon = "fas fa-upload", inputs = {
        @UIActionInput(name = "algorithm", type = Type.select, value = "SSH2_RSA"),
        @UIActionInput(name = "passphrase", type = Type.text),
        @UIActionInput(name = "bits", type = Type.select)
    })
    public ActionResponseModel generatePrivateKey(EntityContext entityContext, JSONObject params) {
        String passphrase = trimToNull(params.getString("passphrase"));
        String algorithm = params.getString("algorithm");
        int bits = params.getInt("bits");
        String alg = "SSH2_RSA".equals(algorithm) ? SSH2_RSA : "ECDSA".equals(algorithm) ? ECDSA : ED25519;
        SshKeyPair keyPair = SshKeyPairGenerator.generateKeyPair(alg, bits);
        SshPrivateKeyFile kf = SshPrivateKeyFileFactory.create(keyPair, passphrase);

        setJsonData("key_pwd", passphrase);
        setJsonData("key", kf.getFormattedKey());
        setJsonData("fp", keyPair.getPublicKey().getFingerprint());
        setJsonData("alg", keyPair.getPrivateKey().getAlgorithm());
        entityContext.save(this);
        return ActionResponseModel.showSuccess("action.success");
    }

    @SneakyThrows
    @UIContextMenuAction(value = "upload_private_key", icon = "fas fa-upload", inputs = {
        @UIActionInput(name = "privateKey", type = Type.text),
        @UIActionInput(name = "passphrase", type = Type.text)
    })
    public ActionResponseModel uploadPrivateKey(EntityContext entityContext, JSONObject params) {
        String privateKey = params.getString("privateKey");
        String passphrase = trimToNull(params.getString("passphrase"));
        SshPrivateKeyFile keyFile = SshPrivateKeyFileFactory.parse(privateKey.getBytes(StandardCharsets.UTF_8));
        if (keyFile.isPassphraseProtected() && passphrase == null) {
            throw new IllegalArgumentException("Key protected with password");
        }
        // verify password
        SshKeyPair keyPair = keyFile.toKeyPair(passphrase);
        setJsonData("key_pwd", passphrase);
        setJsonData("key", privateKey);
        setJsonData("fp", keyPair.getPublicKey().getFingerprint());
        setJsonData("alg", keyPair.getPrivateKey().getAlgorithm());
        entityContext.save(this);
        return ActionResponseModel.showSuccess("action.success");
    }

    @SneakyThrows
    @UIContextMenuAction(value = "download_public_key", icon = "fas fa-download", inputs = {
        @UIActionInput(name = "comment", type = Type.text)
    })
    public ActionResponseModel downloadPublicKey(EntityContext entityContext, JSONObject params) {
        String privateKey = getJsonData("key");
        SshPrivateKeyFile keyFile = SshPrivateKeyFileFactory.parse(privateKey.getBytes(StandardCharsets.UTF_8));
        SshKeyPair keyPair = keyFile.toKeyPair(getJsonData("key_pwd"));

        String publicKey = SshKeyUtils.getFormattedKey(keyPair.getPublicKey(), params.getString("comment"));
        FileModel publicKeyModel = new FileModel("Public key", publicKey, FileContentType.plaintext, true);
        return ActionResponseModel.showFiles(Collections.singleton(publicKeyModel));
    }

    @SneakyThrows
    @UIContextMenuAction(value = "delete_private_key", icon = "fas fa-trash-can", inputs = {
        @UIActionInput(name = "passphrase", type = Type.text)
    })
    public ActionResponseModel deletePrivateKey(EntityContext entityContext, JSONObject params) {
        String passphrase = trimToNull(params.getString("passphrase"));
        String privateKey = getJsonData("key");
        SshPrivateKeyFile keyFile = SshPrivateKeyFileFactory.parse(privateKey.getBytes(StandardCharsets.UTF_8));
        // validate passphrase
        SshKeyPair keyPair = keyFile.toKeyPair(passphrase);
        setJsonData("key_pwd", null);
        setJsonData("key", null);
        setJsonData("fp", null);
        setJsonData("alg", null);
        entityContext.save(this);
        return ActionResponseModel.showSuccess("action.success");
    }
}
