// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete a contract from EXTCTH and EXTCTD 
// Transaction DelContract
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTNO - Contract Number
 * 
*/


 public class DelContract extends ExtendM3Transaction {
    private final MIAPI mi
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
  
    Integer inCONO
    String inDIVI
    int inCTNO  

  // Constructor 
  public DelContract(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Contract Number
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0      
     }

     // Validate contract header
     Optional<DBContainer> EXTCTH = findEXTCTH(inCONO, inDIVI, inCTNO)
     if(!EXTCTH.isPresent()){
        mi.error("Contract Number doesn't exist")   
        return             
     } else {
        // Delete records 
        deleteEXTCTHRecord() 
        deleteEXTCTDRecord()     
     } 
     
  }
 

  //******************************************************************** 
  // Get EXTCTH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTH(int CONO, String DIVI, int CTNO){  
     DBAction query = database.table("EXTCTH").index("00").build()
     DBContainer EXTCTH = query.getContainer()
     EXTCTH.set("EXCONO", CONO)
     EXTCTH.set("EXDIVI", DIVI)
     EXTCTH.set("EXCTNO", CTNO)
     if(query.read(EXTCTH))  { 
       return Optional.of(EXTCTH)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record in EXTCTH
  //******************************************************************** 
  void deleteEXTCTHRecord(){ 
     DBAction action = database.table("EXTCTH").index("00").build()
     DBContainer EXTCTH = action.getContainer()
     EXTCTH.set("EXCONO", inCONO) 
     EXTCTH.set("EXDIVI", inDIVI) 
     EXTCTH.set("EXCTNO", inCTNO)

     action.readLock(EXTCTH, deleterCallbackEXTCTH)
  }
    
  Closure<?> deleterCallbackEXTCTH = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  
  
  //******************************************************************** 
  // Delete record in EXTCTD
  //******************************************************************** 
  void deleteEXTCTDRecord(){ 
     DBAction action = database.table("EXTCTD").index("10").build()
     DBContainer EXTCTD = action.getContainer()
     EXTCTD.set("EXCONO", inCONO) 
     EXTCTD.set("EXDIVI", inDIVI) 
     EXTCTD.set("EXCTNO", inCTNO)

     action.readAllLock(EXTCTD, 3, deleterCallbackEXTCTD)
  }
    
  Closure<?> deleterCallbackEXTCTD = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }