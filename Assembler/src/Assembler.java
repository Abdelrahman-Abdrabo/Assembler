import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Assembler {
    private static Map<String,Integer> Labels;
    static {
        Labels= new HashMap<>();
    }
    public static void SetLabel(String L , Integer i){
        Labels.put(L,i);
    }
    public void SETLabel(String L , Integer i){
        SetLabel(L ,i);
    }

    //Enums to save all instructions with the op code or the op code and the F code and registers
    private enum RType {
         AND("00000","00"),
         OR("00000","01"),
         XOR("00000","10"),
         EQV("00000","11"),
         ADD("00001","00"),
         SUB("00001","01"),
         SLT("00001","10"),
         SEQ("00001","11");
         private final String op;
         private final String F;
         RType(String op ,String F){
             this.op=op;
             this.F=F;
         }
         public String getOp(){
             return this.op;
         }
         public String getF(){
             return this.F;
         }

     }
    private enum IType {
        ANDI("00100"), //4
        ORI("00101"),  //5
        XORI("00110"), //6
        EQVI("00111"), //7
        ADDI("01000"), //8
        SLTI("01001"), //9
        SEQI("01010"), //10
        SLL("01011"), //11
        SRL("01100"), //12
        ROR("01101"), //13
        BEQ("01110"), //14
        BNE("01111"), //15
        LW("10000"), //16
        SW("10001"); //17

        private final String op;

        IType(String op ){
            this.op=op;

        }
        public String getOp(){
            return this.op;
        }

    }
    private enum BType {
        BEQZ("10100"),
        BNEZ("10101"),
        BLTZ("10110"),
        BGEZ("10111"),
        BGTZ("11000"),
        BLEZ("11001"),
        JR("11010"),
        JALR ("11011"),
        SET("11100"),
        SSET("11101");

        private final String op;

        BType(String op ){
            this.op=op;

        }
        public String getOp(){
            return this.op;
        }

    }
    private enum JType {
        J("11110"),
        JAL("11111");
        private final String op;

        JType(String op ){
            this.op=op;

        }
        public String getOp(){
            return this.op;
        }
    }
    private enum Register{
        r0("000"),
        r1("001"),
        r2("010"),
        r3("011"),
        r4("100"),
        r5("101"),
        r6("110"),
        r7("111");
        private final String bin;

        Register(String bin ){
            this.bin=bin;

        }
        public String getbin(){
            return this.bin;
        }
    }
    private String intToBinaryString(int value, int length) {
        String binary = Integer.toBinaryString(value);
        if(binary.length()>length){
            binary=binary.substring(binary.length()-length);
        }
        int padding = length - binary.length();
        if (padding > 0) {
            char[] zeros = new char[padding];
            Arrays.fill(zeros, '0');
            return new String(zeros) + binary;
        }
        return binary;
    }
    private String binaryToHex(StringBuffer binary) {
        System.out.println(binary);
        String hex="";
        for(int i =0 ; i<4 ;i++){
            int decimal = Integer.parseInt(String.valueOf(binary).substring(i*4,i*4+4), 2);
            hex += Integer.toHexString(decimal);
        }
        return hex;
    }
    private char detectType(String ins, Integer I){
        char type=' ';
        String instruction = ins.split("\\s")[0].toUpperCase();
        try {
            RType.valueOf(instruction);
            type='R';
        }catch (Exception e){
            try{
                IType.valueOf(instruction);
                type='I';
            }catch (Exception e1){
                try{
                    BType.valueOf(instruction);
                    type='B';
                }catch (Exception e2){
                    try{
                        JType.valueOf(instruction);
                        type='J';
                    }catch (Exception e3){
                        System.out.println("Invalid instruction in line "+ I +"\ninstruction ("+ins.split("\\s")[0] +") is not on the instruction set, please check again" );
                    }
                }
            }
        }
        return type;
    }
    // R-Type
    private StringBuffer assembleRType(String instruction){
        StringBuffer Binary =new StringBuffer();
        String[] ins=instruction.split("\\s");
        Binary.append(RType.valueOf(ins[0].toUpperCase()).getOp());
        Binary.append((Register.valueOf(ins[1].split("[\\s ,]")[0].toLowerCase()).getbin()));
        Binary.append((Register.valueOf(ins[1].split("[\\s ,]")[1].toLowerCase()).getbin()));
        Binary.append((Register.valueOf(ins[1].split("[\\s ,]")[2].toLowerCase()).getbin()));
        Binary.append(RType.valueOf(ins[0].toUpperCase()).getF());
        return Binary;
    }
    // I-Type
    private StringBuffer assembleIType(String instruction, Integer I){  //the integer is the address of the instruction
        StringBuffer Binary =new StringBuffer();
        String tokens[] = instruction.split("[\\s ,]");
        Binary.append(IType.valueOf(tokens[0].toUpperCase()).getOp());
        Binary.append(Register.valueOf(tokens[1].toLowerCase()).getbin());
        Binary.append(Register.valueOf(tokens[2].toLowerCase()).getbin());
        if (tokens[0].toUpperCase().equals("BEQ")||tokens[0].toUpperCase().equals("BNE")) {
            Integer Offset =Labels.get(tokens[3])-I-1;            //it's a label
            Binary.append(intToBinaryString(Offset,5));
        }else{  // it's a imm value
            if(Integer.parseInt(tokens[3])>31||Integer.parseInt(tokens[3])<-32){
                System.out.println("Invalid Offset in line "+ I +"\nInstruction ("+instruction +") has Invalid offset, please check again.");
            }else{
                Binary.append(intToBinaryString(Integer.parseInt(tokens[3]),5));
            }

        }
        return Binary;
    }
    // B-Type
    private StringBuffer assembleBType(String instruction, Integer I){  //the integer is the address of the instruction
        StringBuffer Binary =new StringBuffer();
        String tokens[] = instruction.split("[\\s ,]");
        Binary.append(BType.valueOf(tokens[0].toUpperCase()).getOp());
        Binary.append(Register.valueOf(tokens[1].toLowerCase()).getbin());
        switch (tokens[0].toUpperCase()) {
            case "BEQZ", "BNEZ", "BLTZ", "BGEZ", "BGTZ", "BLEZ" -> {
                Integer Offset = Labels.get(tokens[2]) - I-1;                               //it's a label
                Binary.append(intToBinaryString(Offset, 8));
            }
            default -> {
                if (Integer.parseInt(tokens[2]) > 255 || Integer.parseInt(tokens[2]) < -256) {
                    System.out.println("Invalid Value in line " + I + "\nInstruction (" + instruction + ") has Invalid value, please check again.");
                } else {
                    Binary.append(intToBinaryString(Integer.parseInt(tokens[2]), 8));
                }
            }
        }

        return Binary;
    }
    // J-Type
    private StringBuffer assembleJType(String instruction, Integer I){  //the integer is the address of the instruction
        StringBuffer Binary =new StringBuffer();
        String tokens[] = instruction.split("[\\s ,]");
        Binary.append(JType.valueOf(tokens[0].toUpperCase()).getOp());
        Integer Offset = Labels.get(tokens[1]) - I;                               //it's a label
        Binary.append(intToBinaryString(Offset, 11));
        return Binary;
    }
    public String assemble(String instruction , Integer I){
        try{
            char Type= detectType(instruction,I);
            return switch (Type) {
                case ('R') -> binaryToHex(assembleRType(instruction));
                case ('I') -> binaryToHex(assembleIType(instruction, I));
                case ('B') -> binaryToHex(assembleBType(instruction, I));
                case ('J') -> binaryToHex(assembleJType(instruction,I));
                default -> "Error in line "+I;
            };
        }catch (Exception e){
            return "Error in line "+I+"\n" +e.getMessage();
        }

    }
}
