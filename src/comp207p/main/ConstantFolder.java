package comp207p.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

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
	
	public void optimize()
	{
		ClassGen cgen = new ClassGen(original);
		ConstantPoolGen cpgen = cgen.getConstantPool();

		// Implement your optimization here
		ConstantPool cp = cpgen.getConstantPool(); //get current constant pool
		Constant[] constants = cp.getConstantPool(); //get constants in the pool

        Method[] methods = cgen.getMethods();

        for (int m = 0; m < methods.length; ++m) {
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
                System.out.println("\t" + current + "\t" + current.toString(cp) + "\topcode " + op);
                if (op >= 2 && op <= 8) {
                    //stack.push( constant );
                    System.out.println("Iconst found");
                }
                else if (op == 96) {
                    System.out.println("2 previous instructions ===== " + ilist.getInstructions()[i-1] );
                    System.out.println("Adding 2 integers");
                }
                else if (op >= 12 && op <= 14) {
                    System.out.println("Loading constant from pool");
                }
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