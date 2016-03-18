package comp207p.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.Repository;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.util.InstructionFinder;

public class ConstantFolder
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	public ConstantFolder(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
    private void pop2(Stack<Number> stack) {
        boolean twice = !(stack.peek() instanceof Long || stack.peek() instanceof Double);
        stack.pop();
        if (twice) {
            stack.pop();
        }
    }
	
	public void optimize()
	{
		ClassGen cgen = new ClassGen(original);
		ConstantPoolGen cpgen = cgen.getConstantPool();

		// Implement your optimization here
		ConstantPool cp = cpgen.getConstantPool(); //get current constant pool
		Constant[] constants = cp.getConstantPool(); //get constants in the pool


        Method[] methods = cgen.getMethods();

        for (int m = 0; m < methods.length; ++m) {
            Stack<Number> stack = new Stack<Number>();
            MethodGen mgen = new MethodGen(methods[m], original.getClassName(), cpgen);

            InstructionList ilist = mgen.getInstructionList();
            InstructionFinder ifinder = new InstructionFinder(ilist);
            // Regex pattern to find using ifinder
            //String pat = "";
            //Stack stack = new Stack();
            System.out.println(original.getClassName() + ": Method "+ m + "/" + methods.length +
                    " : " + methods[m].getName() + " : " + ilist.getLength() + " instructions");
            for (int i = 0; i < ilist.getLength(); ++i) {
                Instruction current = ilist.getInstructions()[i];

                short op = current.getOpcode();
                if (current instanceof IndexedInstruction) {
                    int index = ((IndexedInstruction) current).getIndex();
                    switch(op) {
                        case 0x10:
                            stack.push(index);
                            break;
                    }
                }

                System.out.println("\t" + current + "\t" + current.toString(cp) + "\topcode " + op);

                switch(op) {
                    case 0x09: stack.push(new Long(0)); break;
                    case 0x0a: stack.push(new Long(1)); break;
                    case 0x57: stack.pop(); break;
                    case 0x58: pop2(stack); break;
                    case 0x96: System.out.println("Adding 2 integers"); break;
                    case 0x01: stack.push(null); break;

                    case 0x0e: stack.push(new Double(0.0)); break;
                    case 0x0f: stack.push(new Double(1.0)); break;
                    case 0x59: stack.push(stack.peek()); break;

                }
                if (op >= 0x02 && op <= 0x08) {
                    System.out.println("Iconst found");
                    stack.push(op - 3);
                }
                else if (op >= 0x26 && op <= 0x29) {
                    stack.push(op-26);
                }
                else if (op >= 0x12 && op <= 0x14) {
                    System.out.println("Loading constant from pool");
                }
            }
            System.out.println("Stack");
            for (int i = 0; i < stack.size(); ++i) {
                System.out.println(stack.get(i));
            }
        }


		for(int i=0; i<constants.length; i++) {
			if (constants[i] instanceof ConstantString) {
				ConstantString cs = (ConstantString) constants[i];
                cp.setConstant(cs.getStringIndex(), new ConstantUtf8("HOT DOG"));
			}
		}

		this.optimized = gen.getJavaClass();


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