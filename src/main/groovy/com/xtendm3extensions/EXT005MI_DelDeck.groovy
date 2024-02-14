// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to delete a deck from EXTDPH
// Transaction DelDeck
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPID - Deck ID
 * 
*/


 public class DelDeck extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelDeck(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Deck ID
     int inDPID    
     if (mi.in.get("DPID") != null) {
        inDPID = mi.in.get("DPID") 
     } else {
        inDPID = 0    
     }


     // Validate deck record
     Optional<DBContainer> EXTDPH = findEXTDPH(inCONO, inDIVI, inDPID)
     if(!EXTDPH.isPresent()){
        mi.error("Deck doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTDPHRecord(inCONO, inDIVI, inDPID) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTDPH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDPH(int CONO, String DIVI, int DPID){  
     DBAction query = database.table("EXTDPH").index("00").build()
     DBContainer EXTDPH = query.getContainer()
     EXTDPH.set("EXCONO", CONO)
     EXTDPH.set("EXDIVI", DIVI)
     EXTDPH.set("EXDPID", DPID)
     if(query.read(EXTDPH))  { 
       return Optional.of(EXTDPH)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDPH
  //******************************************************************** 
  void deleteEXTDPHRecord(int CONO, String DIVI, int DPID){ 
     DBAction action = database.table("EXTDPH").index("00").build()
     DBContainer EXTDPH = action.getContainer()
     EXTDPH.set("EXCONO", CONO)
     EXTDPH.set("EXDIVI", DIVI)
     EXTDPH.set("EXDPID", DPID)

     action.readLock(EXTDPH, deleterCallbackEXTDPH)
  }
    
  Closure<?> deleterCallbackEXTDPH = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }