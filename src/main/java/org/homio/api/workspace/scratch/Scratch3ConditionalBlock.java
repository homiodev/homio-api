package org.homio.api.workspace.scratch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@SuppressWarnings({"unused", "unchecked", "rawtypes"})
@Getter
public class Scratch3ConditionalBlock extends Scratch3Block {

    Scratch3ConditionalBlock(int order, String opcode, BlockType blockType, String text, Scratch3BlockHandler handler,
        Scratch3BlockEvaluateHandler evaluateHandler) {
        super(order, opcode, blockType, new ArrayList<>(Collections.singletonList(text)), handler, evaluateHandler);
    }

    public Scratch3ConditionalBlock addBranch(String branchText) {
        ((List) text).add(branchText);
        return this;
    }

    public int getBranchCount() {
        return ((List) text).size();
    }
}
