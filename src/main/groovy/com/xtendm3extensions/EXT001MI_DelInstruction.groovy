// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete an instruction from EXTINS
// Transaction DelInstruction
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: INIC - Instruction Code
 * 
*/


 public class DelInstruction extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database
    private final ProgramAPI program
    private final UtilityAPI utility
    private final LoggerAPI logger
  
    int inCONO
    String inINIC
    
    // Constructor 
    public DelInstruction(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
      this.mi = mi
      this.database = database 
      this.utility = utility
      this.program = program
      this.logger = logger
    }  
    
    public void main() { 
      // Set Company Number
      inCONO = program.LDAZD.CONO as Integer

      // Instruction Code
      if (mi.in.get("INIC") != null && mi.in.get("INIC") != "") {
        inINIC = mi.inData.get("INIC").trim() 
      } else {
        inINIC = ""     
      }

      // Validate instruction record
      Optional<DBContainer> EXTINS = findEXTINS(inCONO, inINIC)
      if(!EXTINS.isPresent()){
        mi.error("Instruction Code doesn't exist")   
        return             
      } else {
        // Delete records 
        deleteEXTINSRecord() 
      } 
     
    }
 

    //******************************************************************** 
    // Get EXTINS record
    //******************************************************************** 
    private Optional<DBContainer> findEXTINS(int CONO, String INIC){  
      DBAction query = database.table("EXTINS").index("00").build()
      DBContainer EXTINS = query.getContainer()
      EXTINS.set("EXCONO", CONO)
      EXTINS.set("EXINIC", INIC)
      if(query.read(EXTINS))  { 
        return Optional.of(EXTINS)
      } 
  
      return Optional.empty()
    }
  

    //******************************************************************** 
    // Delete record from EXTINS
    //******************************************************************** 
    void deleteEXTINSRecord(){ 
      DBAction action = database.table("EXTINS").index("00").build()
      DBContainer EXTINS = action.getContainer()
      EXTINS.set("EXCONO", inCONO) 
      EXTINS.set("EXINIC", inINIC)
      action.readLock(EXTINS, deleterCallbackEXTINS)
    }
    
    Closure<?> deleterCallbackEXTINS = { LockedResult lockedResult ->  
      lockedResult.delete()
    }
  

 }