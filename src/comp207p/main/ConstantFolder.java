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

public class ConstantFolder
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	public ConstantFolder(String classFilePath) {
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}

    public int parseBiSipush(String bipush) {
        String result = "";
        int i=7;
        while (i < bipush.length()) {
            result = result + bipush.charAt(i);
            i++;
        }
        int result_int = Integer.parseInt(result);
        return result_int;
    }

    private void pop2(Stack<Object> stack) {
        boolean twice = !(stack.peek() instanceof Long || stack.peek() instanceof Double);
        stack.pop();
        if (twice) {
            stack.pop();
        }
    }

    private void peekpop2(Stack<Object> stack){
        stack.pop();
        stack.pop();
    }

	public void optimize()
	{
		ClassGen cgen = new ClassGen(original);
		ConstantPoolGen cpgen = cgen.getConstantPool();

		// Implement your optimization here
		ConstantPool cp = cpgen.getConstantPool(); //get current constant pool
		Constant[] constants = cp.getConstantPool(); //get constants in the pool
		HashMap localvars = new HashMap();

        Method[] methods = cgen.getMethods();

        for (int m = 0; m < methods.length; ++m) {

            Stack<Object> stack = new Stack<Object>();
            MethodGen mgen = new MethodGen(methods[m], original.getClassName(), cpgen);

            InstructionList ilist = mgen.getInstructionList();
            InstructionFinder ifinder = new InstructionFinder(ilist);
            InstructionHandle[] handles = ilist.getInstructionHandles();
            for (int i = 0; i < handles.length; i++) {
                    System.out.println("===HANDLE=== " + handles[i]);
            }
            // Regex pattern to find instructions using ifinder: NEED TO DEFINE THE PATTERN!
//            String pattern = "";
//
//            for(Iterator i = ifinder.search(pattern); i.hasNext(); ) {
//                InstructionHandle[] match = (InstructionHandle[])i.next();
//                //do something with the instruction that is found, like delete it:
//                //il.delete(...);
//            }

            System.out.println(original.getClassName() + ": Method "+ m + "/" + methods.length +
                    " : " + methods[m].getName() + " : " + ilist.getLength() + " instructions");

            for (int i = 0; i < ilist.getLength(); ++i) {
                Instruction current = ilist.getInstructions()[i];

                short op = current.getOpcode();
                if (current instanceof IndexedInstruction) {
                    int index = ((IndexedInstruction) current).getIndex();
                    switch(op) {
                        //storing anything into hashmap of local variables //TODO: reference
                        case 0x3a:
                        case 0x38:
                        case 0x36:
                        case 0x37:
                        case 0x39: localvars.put(index, stack.peek()); break; //TODO: pop? why?
                    }
                    if (op >= 0x12 && op <= 0x14) { //Push constant[#index] from constant pool
                        stack.push(cp.getConstant(index));
                        System.out.println("\t>>>Pushing constant onto the stack: " + cp.getConstant(index));
                    }
                    else if (op >= 0x15 && op <= 0x19){ //Load from local var[#index]
                        stack.push((Number)localvars.get(index));
                    }
                    else if (op >= 0x30 && op <= 0x35) { //Load __ from array[#index]
    //                    stack.push();
                    }
                    //REFERENCES
//                    case 0x32:
//                        int index = stack.peek(); stack.pop();
//                        reference arrayref = stack.peek(); stack.pop();
//                        stack.push(arrayref[index].reference); break;

                }

                System.out.println("\t" + current + "\t" + current.toString(cp));

                switch(op) {
                    case 0x57: stack.pop(); break;
                    case 0x58: pop2(stack); break;
                    case 0x01: stack.push(null); break;
                    case 0x59: stack.push(stack.peek()); break;

                    case 0x0e: stack.push(new Double(0.0)); break;
                    case 0x0f: stack.push(new Double(1.0)); break;
                    case 0x09: stack.push(new Long(0)); break;
                    case 0x0a: stack.push(new Long(1)); break;

                    //adding two numbers
                    case 0x63: printStack(stack);stack.push((double)stack.pop() + (double)stack.pop()); break;
                    case 0x62: stack.push((float)stack.pop() + (float)stack.pop()); break;
                    case 0x61: stack.push((long)stack.pop() + (long)stack.pop()); break;
                    case 0x60: stack.push((int)stack.pop() + (int)stack.pop()); break;

                    //multiplying two numbers
                    case 0x6b: stack.push((double)stack.pop() * (double)stack.pop()); break;
                    case 0x6a: stack.push((float)stack.pop() * (float)stack.pop()); break;
                    case 0x69: stack.push((long)stack.pop() * (long)stack.pop()); break;
                    case 0x68: stack.push((int)stack.pop() * (int)stack.pop()); break;

                    //subtracting two numbers
                    case 0x67: stack.push(-(double)stack.pop() + (double)stack.pop()); break;
                    case 0x66: stack.push(-(float)stack.pop() + (float)stack.pop()); break;
                    case 0x65: stack.push(-(long)stack.pop() + (long)stack.pop()); break;
                    case 0x64: stack.push(-(int)stack.pop() + (int)stack.pop()); break;

                    //dividing two numbers
                    case 0x6f: stack.push(1/(double)stack.pop() * (double)stack.pop()); break;
                    case 0x6e: stack.push(1/(float)stack.pop() * (float)stack.pop()); break;
                    case 0x6d: stack.push(1/(long)stack.pop() * (long)stack.pop()); break;
                    case 0x6c: stack.push(1/(int)stack.pop() * (int)stack.pop()); break;

                    //bipush and sipush
                    case 0x10:
                    case 0x11: stack.push(parseBiSipush(current.toString(cp)));
                        System.out.println("\t>>> Pushing value onto stack: " + parseBiSipush(current.toString(cp))); break;

                    //LOADING REFERENCE FROM LOCAL VARIABLES
                    case 0x2a: stack.push(localvars.get(0));break;
                    case 0x2b: stack.push(localvars.get(1));break;
                    case 0x2c: stack.push(localvars.get(2));break;
                    case 0x2d: stack.push(localvars.get(3));break;

                    //storing anything into 0 of hashmap local variables
                    case 0x4b: 
                    case 0x47:
                    case 0x43:
                    case 0x3b: 
                    case 0x3f: localvars.put(0, stack.peek());
                        System.out.println("\t>>> Popping value off the stack " + stack.peek());
                        stack.pop(); break;

                    //storing anything into 1 of hashmap local variables
                    case 0x4c: 
                    case 0x48:
                    case 0x44:
                    case 0x3c: 
                    case 0x40: localvars.put(1, stack.peek());
                        System.out.println("\t>>> Popping value off the stack " + stack.peek());
                        stack.pop(); break;

                    //storing anything into 2 of hashmap local variables
                    case 0x4d: 
                    case 0x49:
                    case 0x45:
                    case 0x3d: 
                    case 0x41: localvars.put(2, stack.peek()); stack.pop(); break;

                    //storing anything into 3 of hashmap local variables
                    case 0x4e: 
                    case 0x4a:
                    case 0x46:
                    case 0x3e: 
                    case 0x42: localvars.put(3, stack.peek()); stack.pop(); break;
                }

                if (op >= 0x02 && op <= 0x08) { //Load int
                    System.out.println("\t>>> Iconst found");
                    stack.push(op - 3);
                }
                //LOADING NUMBER (int, long, float, double) FROM LOCAL VARIABLES
                else if (op == 0x26 || op == 0x22 || op == 0x1a || op == 0x1e) {
                    stack.push(localvars.get(0));
                }
                else if (op == 0x27 || op == 0x23 || op == 0x1b || op == 0x1f) {
                    System.out.println("\t>>> Pushing value onto stack " +  localvars.get(1));
                    stack.push(localvars.get(1));
                }
                else if (op == 0x28 || op == 0x24 || op == 0x1c || op == 0x20) {
                    stack.push((Number)localvars.get(2));
                }
                else if (op == 0x29 || op == 0x25 || op == 0x1d || op == 0x21) {
                    stack.push((Number)localvars.get(3));
                }
            }

        printStack(stack);
        }


		for(int i=0; i<constants.length; i++) {
			if (constants[i] instanceof ConstantString) {
				ConstantString cs = (ConstantString) constants[i];
                cp.setConstant(cs.getStringIndex(), new ConstantUtf8("HOT DOG"));
			}
		}
		this.optimized = gen.getJavaClass();
	}

    public void printStack(Stack<Object> stack) {
        System.out.println("Stack");
        for (int i = 0; i < stack.size(); ++i) {
            System.out.println(stack.get(i));
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