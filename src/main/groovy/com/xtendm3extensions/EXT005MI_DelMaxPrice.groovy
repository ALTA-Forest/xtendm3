// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-02-09
// @version   1.0 
//
// Description 
// This API is to delete a max price from EXTMAX
// Transaction DelMaxPrice
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * 
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTYP - Contract Type
 * @param: RTPC - Rate Type
 * 
*/


 public class DelMaxPrice extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
    // Constructor 
    public DelMaxPrice(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
  
       // Contract Type
       int inCTYP
       if (mi.in.get("CTYP") != null) {
          inCTYP = mi.in.get("CTYP") 
       } else {
          inCTYP = 0       
       }
       
       // Rate Type
       String inRTPC
       if (mi.in.get("RTPC") != null) {
          inRTPC = mi.inData.get("RTPC").trim() 
       } else {
          inRTPC = ""        
       }
    
       // Validate max price record
       Optional<DBContainer> EXTMAX = findEXTMAX(inCONO, inDIVI, inCTYP, inRTPC)
       if(!EXTMAX.isPresent()){
          mi.error("Max Price doesn't exists")   
          return             
       } else {
          // Delete records 
          deleteEXTMAXRecord(inCONO, inDIVI, inCTYP, inRTPC)
       } 
       
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
    // Delete record from EXTMAX
    //******************************************************************** 
    void deleteEXTMAXRecord(int CONO, String DIVI, int CTYP, String RTPC){  
       DBAction action = database.table("EXTMAX").index("00").build()
       DBContainer EXTMAX = action.getContainer()
       EXTMAX.set("EXCONO", CONO)
       EXTMAX.set("EXDIVI", DIVI)
       EXTMAX.set("EXCTYP", CTYP)
       EXTMAX.set("EXRTPC", RTPC)

       action.readLock(EXTMAX, deleterCallbackEXTMAX)
    }
      
    Closure<?> deleterCallbackEXTMAX = { LockedResult lockedResult ->  
       lockedResult.delete()
    }
    

 }