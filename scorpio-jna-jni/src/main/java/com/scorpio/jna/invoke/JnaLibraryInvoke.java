package com.scorpio.jna.invoke;

import com.scorpio.jna.lib.JnaLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class JnaLibraryInvoke {

    public static void main(String[] args) {
        try {
            // lib_init
            Pointer pointer = new Pointer(1);
            int code = JnaLibrary.INSTANCE.lib_init(pointer);

            // lib_parse_file
            int i = JnaLibrary.INSTANCE.lib_parse_file("", 1);

            // int lib_get_message(char msgId,  PointerByReference ppstBody, IntByReference puiNum);
            IntByReference intByReference = new IntByReference();
            PointerByReference pointerByReference = new PointerByReference();

            JnaLibrary.INSTANCE.lib_get_message((char) 1 , pointerByReference, intByReference);

            int count = intByReference.getValue();

            JnaLibrary.MsgStructure structureResult = Structure.newInstance(JnaLibrary.MsgStructure.class,
                    pointerByReference.getValue());
            structureResult.read();
            Structure[] structures = structureResult.toArray(count);

            for(Structure structure :structures){
                JnaLibrary.MsgStructure msg =  (JnaLibrary.MsgStructure) structure;
                System.out.println(msg);
            }

        } catch (Exception e) {
        }
    }
}
