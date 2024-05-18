import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class disassembler {
    public static HashMap<Integer, String> opCodes = new HashMap<>();

    public static HashMap<Integer, String> conditions = new HashMap<>();

    public static int instructionCount = 1;

    public static void main(String[] args) {
        initializeOpCodes();
        initializeConditions();
        if (args.length > 0) {
            readInstructionFromFile(args[0]);
        }
    }
    
    private static void readInstructionFromFile(String fileName) {
        try {
            File instructionsFile = new File(fileName);
            DataInputStream readInstructions = new DataInputStream(new BufferedInputStream(new FileInputStream(instructionsFile)));

            while (readInstructions.available() >= 4) {
                int instruction = assembleInstruction(readInstructions);
                disassemble(instruction);
            }
            readInstructions.close();
        } catch (IOException error) {
            System.out.println(error);
        }
    }

    private static int assembleInstruction(DataInputStream dataInputStream) throws IOException {
        int firstByte = (dataInputStream.readByte() & 0xFF) << 24;
        int secondByte = (dataInputStream.readByte() & 0xFF) << 16;
        int thirdByte = (dataInputStream.readByte() & 0xFF) << 8;
        int fourthByte = (dataInputStream.readByte() & 0xFF);
        return firstByte + secondByte + thirdByte + fourthByte;
    }

	private static void initializeConditions() {
        conditions.put(0xc, "GT");
        conditions.put(0xd, "LE");
        conditions.put(0x0, "EQ");
        conditions.put(0x1, "NE");
        conditions.put(0x2, "HS");
        conditions.put(0x3, "LO");
        conditions.put(0x4, "MI");
        conditions.put(0x5, "PL");
        conditions.put(0x6, "VS");
        conditions.put(0x7, "VC");
        conditions.put(0x8, "HI");
        conditions.put(0x9, "LS");
        conditions.put(0xa, "GE");
        conditions.put(0xb, "LT");
	}

	private static void initializeOpCodes() {
		opCodes.put(0b10001010000, "AND");
        opCodes.put(0b1001001000, "ANDI");
        opCodes.put(0b10001011000, "ADD");
        opCodes.put(0b1001000100, "ADDI");
        opCodes.put(0b000101, "B");
        opCodes.put(0b100101, "BL");
        opCodes.put(0b01010100, "B.");
        opCodes.put(0b11010110000, "BR");
        opCodes.put(0b10110100, "CBZ");
        opCodes.put(0b10110101, "CBNZ");
        opCodes.put(0b1101001000, "EORI");
        opCodes.put(0b11001010000, "EOR");
        opCodes.put(0b11010011011, "LSL");
        opCodes.put(0b11111000010, "LDUR");
        opCodes.put(0b11010011010, "LSR");
        opCodes.put(0b1011001000, "ORRI");
        opCodes.put(0b10101010000, "ORR");
        opCodes.put(0b11111000000, "STUR");
        opCodes.put(0b11001011000, "SUB");
        opCodes.put(0b1101000100, "SUBI");
        opCodes.put(0b1111000100, "SUBIS");
        opCodes.put(0b11101011000, "SUBS");
        opCodes.put(0b10011011000, "MUL");
        opCodes.put(0b11111111101, "PRNT");
        opCodes.put(0b11111111100, "PRNL");
        opCodes.put(0b11111111110, "DUMP");
        opCodes.put(0b11111111111, "HALT");
	}
	
	public static void disassemble(int instr){
        String instrStr = "";

        int rdOpcode = (instr >> 21) & 0x7FF;
        int iOpcode = (instr >> 22) & 0x3FF;
        int cbOpcode = (instr >> 24) & 0xFF;
        int bOpcode = (instr >> 26) & 0x3F;

        if(opCodes.containsKey(rdOpcode)){
        	instrStr = dissambeRDType(rdOpcode, instr, instrStr);
        }
        else if(opCodes.containsKey(iOpcode)){
        	instrStr = disassembleIType(iOpcode, instr, instrStr);
        }
        else if(opCodes.containsKey(cbOpcode)){
        	instrStr = disassembleCBType(cbOpcode, instr, instrStr);
        }
        else if(opCodes.containsKey(bOpcode)){
        	instrStr = disassembleBType(bOpcode, instr, instrStr);
        }
        else{
            System.out.println("Opcode not found");
        }

        System.out.println(instructionCount + ": " + instrStr);
        instructionCount++;
    }

	private static String disassembleBType(int B_opcode, int instr, String instrStr) {
	    int offSet = (instr & 0x03FFFFFF) << 2;
	    if ((offSet & 0x08000000) != 0) { 
	        offSet |= 0xFC000000; 
	    }
	    int targetInstruction = instructionCount + (offSet / 4);
	    instrStr += opCodes.get(B_opcode) + " " + targetInstruction;
	    return instrStr;
	}

	private static String disassembleCBType(int CB_opcode, int instruction, String instructionString) {
	    StringBuilder instStringBuilder = new StringBuilder(instructionString);

	    String opcode = opCodes.get(CB_opcode);
	    if (opcode != null) {
	        instStringBuilder.append(opcode);

	        int register = instruction & 0x1F; 

	        if ("CBZ".equals(opcode) || "CBNZ".equals(opcode)) {
	            instStringBuilder.append(" X").append(register).append(", ");
	        }

	        if ("B.".equals(opcode)) { 
	            String conditionString = conditions.get(register);
	            if (conditionString != null) {
	                instStringBuilder.append(conditionString).append(" ");
	            }
	        }

	        int offset = ((instruction >> 5) & 0x7FFFF) << 2;
	        if ((offset & 0x00080000) != 0) { 
	            offset |= 0xFFF00000; 
	        }
	        int targetInstruction = instructionCount + (offset / 4);
	        instStringBuilder.append(targetInstruction);
	    }

	    return instStringBuilder.toString();
	}




	private static String disassembleIType(int I_opcode, int instruction, String instructionString) {
		 instructionString += opCodes.get(I_opcode);

         int Rd = instruction & 0x1F;
         String RdString = "X" + Rd;
         if(Rd == 28){
             RdString = "SP";
         }
         else if(Rd == 29){
             RdString = "FP";
         }
         else if(Rd == 30){
             RdString = "LR";
         }
         else if(Rd == 31) {
             RdString = "XZR";
         }


         int Rn = instruction >> 5 & 0x1F;
         String RnString = "X" + Rn;
         if(Rn == 28){
             RnString = "SP";
         }
         else if(Rn == 29){
             RnString = "FP";
         }
         else if(Rn == 30){
             RnString = "LR";
         }
         else if(Rn == 31){
             RnString = "XZR";
         }

         int ALUImm = instruction >> 10 & 0xFFF;
         if(ALUImm >= 2048){
             ALUImm -= 4096;
         }

         instructionString += " " + RdString + ", " + RnString + ", #" + ALUImm;
		return instructionString;
	}

	private static String dissambeRDType(int R_D_opcode, int instruction, String instructionString) {
		instructionString += opCodes.get(R_D_opcode);

        if((R_D_opcode == 0b10001011000) || (R_D_opcode == 0b10001010000) || (R_D_opcode == 0b11001010000) || (R_D_opcode == 0b10101010000) || (R_D_opcode == 0b11001011000) || (R_D_opcode == 0b11101011000) || (R_D_opcode == 0b10011011000)){
            int Rd = instruction & 0x1F;
            String RdString = "X" + Rd;
            if(Rd == 28){
                RdString = "SP";
            }
            else if(Rd == 29){
                RdString = "FP";
            }
            else if(Rd == 30){
                RdString = "LR";
            }
            else if(Rd == 31){
                RdString = "XZR";
            }


            int Rn = instruction >> 5 & 0x1F;
            String RnString = "X" + Rn;
            if(Rn == 28){
                RnString = "SP";
            }
            else if(Rn == 29){
                RnString = "FP";
            }
            else if(Rn == 30){
                RnString = "LR";
            }
            else if(Rn == 31){
                RnString = "XZR";
            }


            int Rm = instruction >> 16 & 0x1F;
            String RmString = "X" + Rm;
            if(Rm == 28){
                RmString = "SP";
            }
            else if(Rm == 29){
                RmString = "FP";
            }
            else if(Rm == 30){
                RmString = "LR";
            }
            else if(Rm == 31){
                RmString = "XZR";
            }

            instructionString += " " + RdString + ", " + RnString + ", " + RmString;
        }

        else if(R_D_opcode == 0b11010110000){
            int Rn = instruction >> 5 & 0x1F;
            String RnString = "X" + Rn;
            if(Rn == 28){
                RnString = "SP";
            }
            else if(Rn == 29){
                RnString = "FP";
            }
            else if(Rn == 30){
                RnString = "LR";
            }
            else if(Rn == 31){
                RnString = "XZR";
            }

            instructionString += " " + RnString;
        }
        

        else if((R_D_opcode == 0b11010011011) || (R_D_opcode == 0b11010011010)) {
            int Rd = instruction & 0x1F;
            String RdString = "X" + Rd;
            if(Rd == 28){
                RdString = "SP";
            }
            else if(Rd == 29){
                RdString = "FP";
            }
            else if(Rd == 30){
                RdString = "LR";
            }
            else if(Rd == 31){
                RdString = "XZR";
            }

            int Rn = instruction >> 5 & 0x1F;
            String RnString = "X" + Rn;
            if(Rn == 28){
                RnString = "SP";
            }
            else if(Rn == 29){
                RnString = "FP";
            }
            else if(Rn == 30){
                RnString = "LR";
            }
            else if(Rn == 31){
                RnString = "XZR";
            }

            int shamt = instruction >> 10 & 0x3F;
            if(shamt >= 32){
                shamt -= 64;
            }

            instructionString += " " + RdString + ", " + RnString + ", #" + shamt;
        }

        else if(R_D_opcode == 0b11111111101){
            int Rd = instruction & 0x1F;
            String RdString = "X" + Rd;
            if(Rd == 28){
                RdString = "SP";
            }
            else if(Rd == 29){
                RdString = "FP";
            }
            else if(Rd == 30){
                RdString = "LR";
            }
            else if(Rd == 31){
                RdString = "XZR";
            }

            instructionString += " " + RdString;
        }

        else if((R_D_opcode == 0b11111000010) || (R_D_opcode == 0b11111000000)){
            int Rt = instruction & 0x1F;
            String RtString = "X" + Rt;
            if(Rt == 28){
                RtString = "SP";
            }
            else if(Rt == 29){
                RtString = "FP";
            }
            else if(Rt == 30){
                RtString = "LR";
            }
            else if(Rt == 31){
                RtString = "XZR";
            }


            int Rn = instruction >> 5 & 0x1F;
            String RnString = "X" + Rn;
            if(Rn == 28){
                RnString = "SP";
            }
            else if(Rn == 29){
                RnString = "FP";
            }
            else if(Rn == 30){
                RnString = "LR";
            }
            else if(Rn == 31){
                RnString = "XZR";
            }

            int DTAddr = instruction >> 12 & 0x1FF;
            if(DTAddr >= 256){
                DTAddr -= 512;
            }
            
            instructionString += " " + RtString + ", [" + RnString + ", #" + DTAddr + "]";
        }

        else{
        }
        return instructionString;
	}

}



