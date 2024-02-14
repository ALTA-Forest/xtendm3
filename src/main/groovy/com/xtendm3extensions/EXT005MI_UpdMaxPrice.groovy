// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-02-09
// @version   1.0 
//
// Description 
// This API is to update a max price in EXTMAX
// Transaction UpdMaxPrice
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTYP - Contract Type
 * @param: RTPC - Rate Type
 * @param: AMNT - Amount
 * 
*/



public class UpdMaxPrice extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inCTYP
  String inRTPC
  
  // Constructor 
  public UpdMaxPrice(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Contract Type
     if (mi.in.get("CTYP") != null) {
        inCTYP = mi.in.get("CTYP") 
     } else {
        inCTYP = 0       
     }
       
     // Rate Type
     if (mi.in.get("RTPC") != null) {
        inRTPC = mi.inData.get("RTPC").trim() 
     } else {
        inRTPC = ""        
     }
     

     // Validate max price record
     Optional<DBContainer> EXTMAX = findEXTMAX(inCONO, inDIVI, inCTYP, inRTPC)
     if(!EXTMAX.isPresent()){
        mi.error("Max Price doesn't  exist")   
        return             
     }
    
     // Update record
     updEXTMAXRecord()
     
  }

  
  //******************************************************************** 
  // Get EXTMAX record
  //******************************************************************** 
  private Optional<DBContainer> findEXTMAX(int CONO, String DIVI, int CTYP, String RTPC){  
     DBAction query = database.table("EXTMAX").index("00").build()
     DBContainer EXTMAX = query.getContainer()
     EXTMAX.set("EXCONO", CONO)
     EXTMAX.set("EXDIVI", DIVI)
     EXTMAX.set("EXCTYP", CTYP)
     EXTMAX.set("EXRTPC", RTPC)
     if(query.read(EXTMAX))  { 
       return Optional.of(EXTMAX)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Update EXTMAX record
  //********************************************************************    
  void updEXTMAXRecord(){      
     DBAction action = database.table("EXTMAX").index("00").build()
     DBContainer EXTMAX = action.getContainer()    
     EXTMAX.set("EXCONO", inCONO)
     EXTMAX.set("EXDIVI", inDIVI)
     EXTMAX.set("EXCTYP", inCTYP)
     EXTMAX.set("EXRTPC", inRTPC)

     // Read with lock
     action.readLock(EXTMAX, updateCallBackEXTMAX)
     }
   
     Closure<?> updateCallBackEXTMAX = { LockedResult lockedResult -> 
       if (mi.in.get("AMNT") != null) {
          lockedResult.set("EXAMNT", mi.in.get("AMNT"))
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

