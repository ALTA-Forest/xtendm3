// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to delete deck type from EXTDPT
// Transaction DelDeckType
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: TYPE - Deck Type
 * 
*/


 public class DelDeckType extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelDeckType(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database 
     this.program = program
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
     String inTYPE     
     if (mi.in.get("TYPE") != null) {
        inTYPE = mi.in.get("TYPE") 
     } else {
        inTYPE = "" 
     }


     // Validate deck type record
     Optional<DBContainer> EXTDPT = findEXTDPT(inCONO, inDIVI, inTYPE)
     if(!EXTDPT.isPresent()){
        mi.error("Deck type doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTDPTRecord(inCONO, inDIVI, inTYPE) 
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
  // Delete record from EXTDPT
  //******************************************************************** 
  void deleteEXTDPTRecord(int CONO, String DIVI, String TYPE){ 
     DBAction action = database.table("EXTDPT").index("00").build()
     DBContainer EXTDPT = action.getContainer()
     EXTDPT.set("EXCONO", CONO)
     EXTDPT.set("EXDIVI", DIVI)
     EXTDPT.set("EXTYPE", TYPE)

     action.readLock(EXTDPT, deleterCallbackEXTDPT)
  }
    
  Closure<?> deleterCallbackEXTDPT = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }