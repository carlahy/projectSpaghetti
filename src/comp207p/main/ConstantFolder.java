package comp207p.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.Repository;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.util.InstructionFinder;

public class ConstantFolder {

    ClassParser parser = null;
    ClassGen gen = null;

    JavaClass original = null;
    JavaClass optimized = null;

    public ConstantFolder(String classFilePath) {
        try {
            this.parser = new ClassParser(classFilePath);
            this.original = this.parser.parse();
            this.gen = new ClassGen(this.original);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    // Replace Compare Instructions with a PUSH Instruction
    public void optimiseCompareOp(InstructionList ilist, InstructionHandle handle, MethodGen mgen, ConstantPoolGen cpgen) {
        Number value2 = popStack(ilist, handle, cpgen);
        Number value1 = popStack(ilist, handle, cpgen);
        int constant = 0;

        if (handle.getInstruction() instanceof DCMPG) {
            if ((double)value1 > (double)value2) {
                constant = 1;
            } else if ((double)value1 < (double)value2) {
                constant = -1;
            }
            cpgen.addInteger(constant);
            ilist.append(handle, new PUSH(cpgen, constant));
            removeInstruction(ilist, handle);
        } else if (handle.getInstruction() instanceof DCMPL) {
            if ((double)value1 < (double)value2) {
                constant = 1;
            } else if ((double)value1 > (double)value2) {
                constant = -1;
            }
            cpgen.addInteger(constant);
            ilist.append(handle, new PUSH(cpgen, constant));
            removeInstruction(ilist, handle);
        } else if (handle.getInstruction() instanceof FCMPG) {
            if ((float)value1 > (float)value2) {
                constant = 1;
            } else if ((float)value1 < (float)value2) {
                constant = -1;
            }
            cpgen.addInteger(constant);
            ilist.append(handle, new PUSH(cpgen, constant));
            removeInstruction(ilist, handle);
        } else if (handle.getInstruction() instanceof FCMPL) {
            if ((float)value1 < (float)value2) {
                constant = 1;
            } else if ((float)value1 > (float)value2) {
                constant = -1;
            }
            cpgen.addInteger(constant);
            ilist.append(handle, new PUSH(cpgen, constant));
            removeInstruction(ilist, handle);
        } else if (handle.getInstruction() instanceof LCMP) {
            if ((long)value1 > (long)value2) {
                constant = 1;
            } else if ((double)value1 < (double)value2) {
                constant = -1;
            }
            cpgen.addInteger(constant);
            ilist.append(handle, new PUSH(cpgen, constant));
            removeInstruction(ilist, handle);
        }
    }

    // Replace ConversionInstruction with a PUSH Instruction
    public void optimiseNumberConversion(InstructionList ilist, InstructionHandle handle, MethodGen mgen, ConstantPoolGen cpgen) {
        Number constant = popStack(ilist, handle, cpgen);
        if (handle.getInstruction() instanceof I2D
                || handle.getInstruction() instanceof L2D
                || handle.getInstruction() instanceof F2D) {
            try {
                int result = (int)constant;
                cpgen.addDouble((double)result);
                ilist.append(handle, new PUSH(cpgen, (double)result));
                ilist.delete(handle);
            } catch (TargetLostException e) {
                e.printStackTrace();
            }
        } else if (handle.getInstruction() instanceof D2I
                || handle.getInstruction() instanceof L2I
                || handle.getInstruction() instanceof F2I) {
            try {
                cpgen.addInteger((int)constant);
                ilist.append(handle, new PUSH(cpgen, (int)constant));
                ilist.delete(handle);
            } catch (TargetLostException e) {
                e.printStackTrace();
            }
        } else if (handle.getInstruction() instanceof I2L
                || handle.getInstruction() instanceof D2L
                || handle.getInstruction() instanceof F2L) {
            try {
                cpgen.addLong((long)constant);
                ilist.append(handle, new PUSH(cpgen, (long)constant));
                ilist.delete(handle);
            } catch (TargetLostException e) {
                e.printStackTrace();
            }
        } else if (handle.getInstruction() instanceof I2F
                || handle.getInstruction() instanceof L2F
                || handle.getInstruction() instanceof D2F) {
            try {
                cpgen.addFloat((float)constant);
                ilist.append(handle, new PUSH(cpgen, (float)constant));
                ilist.delete(handle);
            } catch (TargetLostException e) {
                e.printStackTrace();
            }
        }

    }

    // Get int value at constant index; increment int value; update constant value at index;
    // Replace iinc with push with reference to constant index;

//    // Replace IINC Instruction with a PUSH Compound Instruction
//    public void optimiseIncrementOp(InstructionList ilist, InstructionHandle handle, MethodGen mgen, ConstantPoolGen cpgen) {
//        int index = ((IINC) handle.getInstruction()).getIndex();
//        int increment = ((IINC) handle.getInstruction()).getIncrement();
//
//        int intconstant = (int)((ConstantInteger)cpgen.getConstant(index)).getBytes();
//        intconstant = intconstant + increment;
//
//        //Updating Constant Pool with incremented integer
//        ConstantInteger newIntConstant = new ConstantInteger(intconstant);
//        cpgen.setConstant(index, newIntConstant);
//
//        ilist.append(handle, new PUSH(cpgen, (int)intconstant));
//        removeInstruction(ilist, handle);
//    }

    // Replace LoadInstruction with a PUSH Instruction
    public void optimiseLoadingOp(InstructionList ilist, InstructionHandle handle, MethodGen mgen, ConstantPoolGen cpgen, Number constant) {
        if (handle.getInstruction() instanceof ILOAD) {
            cpgen.addInteger((int)constant);
            ilist.append(handle, new PUSH(cpgen, (int)constant));
            removeInstruction(ilist, handle);
        } else if (handle.getInstruction() instanceof DLOAD) {
            cpgen.addDouble((double)constant);
            ilist.append(handle, new PUSH(cpgen, (double)constant));
            removeInstruction(ilist, handle);
        } else if (handle.getInstruction() instanceof FLOAD) {
            cpgen.addFloat((float)constant);
            ilist.append(handle, new PUSH(cpgen, (float)constant));
            removeInstruction(ilist, handle);
        } else if (handle.getInstruction() instanceof LLOAD) {
            cpgen.addLong((long)constant);
            ilist.append(handle, new PUSH(cpgen, (long)constant));
            removeInstruction(ilist, handle);
        }
    }

    // Perform arithmetic operation
    public Number evaluateArithmeticOp(InstructionList ilist, InstructionHandle handle, ConstantPoolGen cpgen) {
        if (handle.getInstruction() instanceof DADD) {
            return (double)popStack(ilist, handle, cpgen) + (double)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof DMUL) {
            return (double)popStack(ilist, handle, cpgen) * (double)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof DDIV) {
            return 1/(double)popStack(ilist, handle, cpgen) * (double)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof DSUB) {
            return -(double)popStack(ilist, handle, cpgen) + (double)popStack(ilist, handle, cpgen);
        }

        else if (handle.getInstruction() instanceof FADD) {
            return (float)popStack(ilist, handle, cpgen) + (float)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof FMUL) {
            return (float)popStack(ilist, handle, cpgen) * (float)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof FDIV) {
            return 1/(float)popStack(ilist, handle, cpgen) * (float)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof FSUB) {
            return -(float)popStack(ilist, handle, cpgen) + (float)popStack(ilist, handle, cpgen);
        }

        else if (handle.getInstruction() instanceof IADD) {
            return (int)popStack(ilist, handle, cpgen) + (int)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof IMUL) {
            return (int)popStack(ilist, handle, cpgen) * (int)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof IDIV) {
            return 1/(int)popStack(ilist, handle, cpgen) * (int)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof ISUB) {
            return -(int)popStack(ilist, handle, cpgen) + (int)popStack(ilist, handle, cpgen);
        }

        else if (handle.getInstruction() instanceof LADD) {
            return (long)popStack(ilist, handle, cpgen) + (long)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof LMUL) {
            return (long)popStack(ilist, handle, cpgen) * (long)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof LDIV) {
            return 1/(long)popStack(ilist, handle, cpgen) * (long)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof LSUB) {
            return -(long)popStack(ilist, handle, cpgen) + (long)popStack(ilist, handle, cpgen);
        }

        else if (handle.getInstruction() instanceof INEG) {
            return -(int)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof LNEG) {
            return -(long)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof DNEG) {
            return -(double)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof FNEG) {
            return -(float)popStack(ilist, handle, cpgen);
        }

        else if (handle.getInstruction() instanceof IREM) {
            int value2 = (int)popStack(ilist, handle, cpgen);
            int value1 = (int)popStack(ilist, handle, cpgen);
            return value1 % value2;
        } else if (handle.getInstruction() instanceof LREM) {
            long value2 = (long)popStack(ilist, handle, cpgen);
            long value1 = (long)popStack(ilist, handle, cpgen);
            return  value1 % value2;
        } else if (handle.getInstruction() instanceof DREM) {
            double value2 = (double)popStack(ilist, handle, cpgen);
            double value1 = (double)popStack(ilist, handle, cpgen);
            return  value1 % value2;
        } else if (handle.getInstruction() instanceof FREM) {
            float value2 = (float)popStack(ilist, handle, cpgen);
            float value1 = (float)popStack(ilist, handle, cpgen);
            return  value1 % value2;
        }

        else if (handle.getInstruction() instanceof IOR) {
            return -(int)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof IXOR) {
            return -(long)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof LOR) {
            return -(double)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof LXOR) {
            return -(float)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof IAND) {
            return -(double)popStack(ilist, handle, cpgen);
        } else if (handle.getInstruction() instanceof LAND) {
            return -(float)popStack(ilist, handle, cpgen);
        }
        return null;
    }

    // Replace arithmetic operation with PUSH Instruction
    public void optimiseArithmeticOp(InstructionList ilist, InstructionHandle handle, MethodGen mgen, ConstantPoolGen cpgen) {
        int count = 0;
        Number constant = evaluateArithmeticOp(ilist, handle, cpgen);
        if (constant instanceof Double) {
            try {
                cpgen.addDouble((double)constant);
                ilist.append(handle, new PUSH(cpgen, (double)constant));
                ilist.delete(handle);
                count++;
            } catch (TargetLostException e) {
                e.printStackTrace();
            }

        } else if (constant instanceof Float) {
            try {
                cpgen.addFloat((float)constant);
                ilist.append(handle, new PUSH(cpgen, (float)constant));
                ilist.delete(handle);
                count++;
            } catch (TargetLostException e) {
                e.printStackTrace();
            }

        } else if (constant instanceof Integer) {
            try {
                cpgen.addInteger((int)constant);
                ilist.append(handle, new PUSH(cpgen, (int)constant));
                ilist.delete(handle);
                count++;
            } catch (TargetLostException e) {
                e.printStackTrace();
            }

        } else if (constant instanceof Long) {
            try {
                cpgen.addLong((long)constant);
                ilist.append(handle, new PUSH(cpgen, (long)constant));
                ilist.delete(handle);
                count++;
            } catch (TargetLostException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeInstruction(InstructionList ilist, InstructionHandle handle) {
        ilist.redirectBranches(handle, handle.getPrev());
        try {
            ilist.delete(handle);
        } catch (TargetLostException e) {
            //e.printStackTrace();
        }
    }

    // Get value on top of the stack, and delete the corresponding instruction
    public Number popStack(InstructionList ilist, InstructionHandle handle, ConstantPoolGen cpgen) {
        Number value = null;
        while(true) {
            if (handle.getInstruction() instanceof BIPUSH) {
                value = ((BIPUSH) handle.getInstruction()).getValue();
                removeInstruction(ilist, handle); break;
            } else if (handle.getInstruction() instanceof SIPUSH) {
                value = ((SIPUSH) handle.getInstruction()).getValue();
                removeInstruction(ilist, handle); break;
            } else if (handle.getInstruction() instanceof ICONST) {
                value = ((ICONST) handle.getInstruction()).getValue();
                removeInstruction(ilist, handle);break;
            } else if (handle.getInstruction() instanceof DCONST) {
                value = ((DCONST) handle.getInstruction()).getValue();
                removeInstruction(ilist, handle);break;
            } else if (handle.getInstruction() instanceof LCONST) {
                value = ((LCONST) handle.getInstruction()).getValue();
                removeInstruction(ilist, handle);break;
            } else if (handle.getInstruction() instanceof FCONST) {
                value = ((FCONST) handle.getInstruction()).getValue();
                removeInstruction(ilist, handle);break;
            } else if (handle.getInstruction() instanceof LDC) {
                value = (Number)((LDC) handle.getInstruction()).getValue(cpgen);
                removeInstruction(ilist, handle);break;
            } else if (handle.getInstruction() instanceof LDC2_W) {
                value = (Number)((LDC2_W) handle.getInstruction()).getValue(cpgen);
                removeInstruction(ilist, handle);break;
            } else {
                handle = handle.getPrev();
            }
        }
        return value;
    }

    public void optimizeMethod(ClassGen cgen, ConstantPoolGen cpgen, Method m, int length) {
        ConstantPool cp = cpgen.getConstantPool();
        Constant[] constants = cp.getConstantPool();
        MethodGen mgen = new MethodGen(m, original.getClassName(), cpgen);
        InstructionList ilist = mgen.getInstructionList();

        System.out.println(original.getClassName() + ": Method " + m +
                " : " + ilist.getLength() + " instructions");

        InstructionHandle handle = ilist.getStart();
        printInstructions(ilist, cp);
        while (handle != null) {

            if (handle.getInstruction() instanceof StoreInstruction) {
                int index = ((IndexedInstruction) handle.getInstruction()).getIndex();
                InstructionHandle storeHandle = handle;
                InstructionHandle currentHandle = handle.getNext();
                Number constant = popStack(ilist, storeHandle, cpgen);

                while (!(currentHandle.getInstruction() instanceof StoreInstruction && ((StoreInstruction)currentHandle.getInstruction()).getIndex() == index) && currentHandle != null) {
                    if (currentHandle.getInstruction() instanceof LoadInstruction && ((LoadInstruction)currentHandle.getInstruction()).getIndex() == index) {
                        InstructionHandle loadHandle = currentHandle;
                        currentHandle = currentHandle.getNext();
                        optimiseLoadingOp(ilist, loadHandle, mgen, cpgen, constant);
                    } else {
                        currentHandle = currentHandle.getNext();
                    }
                    if (currentHandle == null) {
                        break;
                    }
                }
                removeInstruction(ilist, storeHandle);
                cgen.replaceMethod(m, mgen.getMethod());
                handle = ilist.getStart();
            }

//            else if (handle.getInstruction() instanceof IINC) {
//                optimiseIncrementOp(ilist, handle, mgen, cpgen);
//                cgen.replaceMethod(m, mgen.getMethod());
//                handle = ilist.getStart();
//            }

            else if (handle.getInstruction() instanceof ArithmeticInstruction) {
                optimiseArithmeticOp(ilist, handle, mgen, cpgen);
                cgen.replaceMethod(m, mgen.getMethod());
                handle = ilist.getStart();
            }

            else if (handle.getInstruction() instanceof ConversionInstruction) {
                optimiseNumberConversion(ilist, handle, mgen, cpgen);
                cgen.replaceMethod(m, mgen.getMethod());
                handle = ilist.getStart();
            }

            // Compare Instructions
            else if (handle.getInstruction() instanceof DCMPG
                    || handle.getInstruction() instanceof DCMPL
                    || handle.getInstruction() instanceof FCMPG
                    || handle.getInstruction() instanceof FCMPL
                    || handle.getInstruction() instanceof LCMP) {
                optimiseCompareOp(ilist, handle, mgen, cpgen);
                cgen.replaceMethod(m, mgen.getMethod());
                handle = ilist.getStart();
            }

            else {
                handle = handle.getNext();
            }
            ilist.setPositions(true);
        }

        // Checks whether jump handles are all within the current method
        ilist.setPositions(true);
        mgen.setMaxStack();
        mgen.setMaxLocals();
        // Generate the new optimised method
        Method newMethod = mgen.getMethod();
        // Replace the method in the original class
        cgen.replaceMethod(m, newMethod);

        // Debugging
        System.out.println("Optimised Method " + m + " to " + ilist.getLength() + " instructions");
        printInstructions(ilist, cp);
        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");

        cgen.setMajor(50);
        this.optimized = gen.getJavaClass();
    }

    private void optimize() {
        // Load the original class into a class generator
        ClassGen cgen = new ClassGen(original);
        ConstantPoolGen cpgen = cgen.getConstantPool();

        // Do optimization here
        Method[] methods = cgen.getMethods();
        for (Method m : methods)
        {
            optimizeMethod(cgen, cpgen, m, methods.length);
        }

        // we generate a new class with modifications
        // and store it in a member variable
        cgen.setMajor(50);
        this.optimized = cgen.getJavaClass();
    }

    public void printInstructions(InstructionList ilist, ConstantPool cp) {
        for (int j = 0; j < ilist.getLength(); ++j) {
            Instruction current = ilist.getInstructions()[j];
            System.out.println("\t" + current);
        }
    }

    public void write(String optimisedFilePath)
    {
        this.optimize();

        try {
            FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
            this.optimized.dump(out);
        } catch (FileNotFoundException e) {
            // Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
    }
}