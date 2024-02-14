// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to update deck type in EXTDPT
// Transaction UpdDeckType
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: TYPE - Deck Type
 * @param: NAME - Name
 * 
*/



public class UpdDeckType extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  String inTYPE
  String inNAME
  
  // Constructor 
  public UpdDeckType(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.utility = utility
     this.logger = logger     
  } 
    
  public void main() {       
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Deck Type
     if (mi.in.get("TYPE") != null) {
        inTYPE = mi.in.get("TYPE") 
     } else {
        inTYPE = ""         
     }
           
     // Name
     if (mi.in.get("NAME") != null) {
        inNAME = mi.in.get("NAME") 
     } else {
        inNAME= ""      
     }


     // Validate Deck Type record
     Optional<DBContainer> EXTDPT = findEXTDPT(inCONO, inDIVI, inTYPE)
     if (!EXTDPT.isPresent()) {
        mi.error("Deck Type doesn't exist")   
        return             
     } else {
        // Update record
        updEXTDPTRecord()
     }
     
  }
  
    
  //******************************************************************** 
  // Get EXTDPT record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDPT(int CONO, String DIVI, String TYPE){  
     DBAction query = database.table("EXTDPT").index("00").build()
     DBContainer EXTDPT = query.getContainer()
     EXTDPT.set("EXCONO", CONO)
     EXTDPT.set("EXDIVI", DIVI)
     EXTDPT.set("EXTYPE", TYPE)
     if(query.read(EXTDPT))  { 
       return Optional.of(EXTDPT)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Update EXTDPT record
  //********************************************************************    
  void updEXTDPTRecord(){      
     DBAction action = database.table("EXTDPT").index("00").build()
     DBContainer EXTDPT = action.getContainer()     
     EXTDPT.set("EXCONO", inCONO)
     EXTDPT.set("EXDIVI", inDIVI)
     EXTDPT.set("EXTYPE", inTYPE)

     // Read with lock
     action.readLock(EXTDPT, updateCallBackEXTDPT)
     }
   
     Closure<?> updateCallBackEXTDPT = { LockedResult lockedResult -> 
       if (inNAME != null && inNAME != "") {
          lockedResult.set("EXNAME", inNAME)
       }
  
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)       
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }

} 

