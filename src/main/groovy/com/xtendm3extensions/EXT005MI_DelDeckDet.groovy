// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to delete deck details from EXTDPD
// Transaction DelDeckDet
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPID - Deck ID
 * 
*/


 public class DelDeckDet extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelDeckDet(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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


     // Validate deck detail record
     Optional<DBContainer> EXTDPD = findEXTDPD(inCONO, inDIVI, inDPID)
     if(!EXTDPD.isPresent()){
        mi.error("Deck doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTDPDRecord(inCONO, inDIVI, inDPID) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTDPD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDPD(int CONO, String DIVI, int DPID){  
     DBAction query = database.table("EXTDPD").index("00").build()
     DBContainer EXTDPD = query.getContainer()
     EXTDPD.set("EXCONO", CONO)
     EXTDPD.set("EXDIVI", DIVI)
     EXTDPD.set("EXDPID", DPID)
     if(query.read(EXTDPD))  { 
       return Optional.of(EXTDPD)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDPD
  //******************************************************************** 
  void deleteEXTDPDRecord(int CONO, String DIVI, int DPID){ 
     DBAction action = database.table("EXTDPD").index("00").build()
     DBContainer EXTDPD = action.getContainer()
     EXTDPD.set("EXCONO", CONO)
     EXTDPD.set("EXDIVI", DIVI)
     EXTDPD.set("EXDPID", DPID)

     action.readLock(EXTDPD, deleterCallbackEXTDPD)
  }
    
  Closure<?> deleterCallbackEXTDPD = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }