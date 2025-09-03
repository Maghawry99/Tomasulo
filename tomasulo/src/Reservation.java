
public class Reservation{
    int size;
    Instruction[] reservationStation;
    String label; //A,M,L,S

    public Reservation( String label, int size) {
        this.size = size;
        this.label = label;
        reservationStation = new Instruction[size];
    }

    public Boolean issueInstruction(Instruction instructionToIssue , int cyclenumber){
        for (int i=0; i<size; i++){
            //System.out.println("*********************************BOS HENAAAAAAAAAAAAAAAAA"+reservationStation[i]);
            if (reservationStation[i] == null){
                reservationStation[i] = instructionToIssue;
                reservationStation[i].issueCycle = cyclenumber;
   
                if (instructionToIssue.operation!="BNEZ" && instructionToIssue.operation != "BNE"&& instructionToIssue.operation !="BEQ"){
                    instructionToIssue.setLabel(label + i);
                }
                return true;
            }
            else if (reservationStation[i].getBusy() == 0){
                reservationStation[i] = instructionToIssue;
                reservationStation[i].issueCycle = cyclenumber;
                if (instructionToIssue.operation!="BNEZ" && instructionToIssue.operation != "BNE" && instructionToIssue.operation !="BEQ"){
                    instructionToIssue.setLabel(label + i);
                }
                return true;
            }
        }
        return false;
    }
   
}