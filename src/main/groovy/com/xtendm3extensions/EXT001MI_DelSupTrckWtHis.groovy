// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete supplier truck weight history from EXTTWH
// Transaction DelSupTrckWtHis
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SUNO - Supplier
 * @param: TRCK - Truck
 * @param: FRDT - From Date
 * @param: TODT - To Date
 * 
*/


 public class DelSupTrckWtHis extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
  
    int inCONO
    String inSUNO
    String inTRCK
    int inFRDT
    int inTODT
    
    // Constructor 
    public DelSupTrckWtHis(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
       this.mi = mi
       this.database = database 
       this.program = program
       this.logger = logger
    } 
      
    public void main() { 
       // Set Company Number
       inCONO = program.LDAZD.CONO as Integer
  
       // Supplier
       if (mi.in.get("SUNO") != null && mi.in.get("SUNO") != "") {
          inSUNO = mi.inData.get("SUNO").trim() 
       } else {
          inSUNO = ""     
       }
  
       // Truck
       if (mi.in.get("TRCK") != null && mi.in.get("TRCK") != "") {
          inTRCK = mi.inData.get("TRCK").trim() 
       } else {
          inTRCK = ""     
       }
       
       // From Date
       if (mi.in.get("FRDT") != null) {
          inFRDT = mi.in.get("FRDT") 
       } else {
          inFRDT = 0        
       }
  
       // To Date
       if (mi.in.get("TODT") != null) {
          inTODT = mi.in.get("TODT") 
       } else {
          inTODT = 0        
       }
  
       // Validate supplier truck weight history record
       Optional<DBContainer> EXTTWH = findEXTTWH(inCONO, inSUNO, inTRCK, inFRDT, inTODT)
       if(!EXTTWH.isPresent()){
          mi.error("Supplier Truck Weight History doesn't exist")   
          return             
       } else {
          // Delete records 
          deleteEXTTWHRecord() 
       } 
       
    }
  
  
  
    //******************************************************************** 
    // Get EXTTWH record
    //******************************************************************** 
    private Optional<DBContainer> findEXTTWH(int CONO, String SUNO, String TRCK, int FRDT, int TODT){  
       DBAction query = database.table("EXTTWH").index("00").build()
       DBContainer EXTTWH = query.getContainer()
       EXTTWH.set("EXCONO", CONO)
       EXTTWH.set("EXSUNO", SUNO)
       EXTTWH.set("EXTRCK", TRCK)
       EXTTWH.set("EXFRDT", FRDT)
       EXTTWH.set("EXTODT", TODT)
       if(query.read(EXTTWH))  { 
         return Optional.of(EXTTWH)
       } 
    
       return Optional.empty()
    }
    
  
    //******************************************************************** 
    // Delete record from EXTTWH
    //******************************************************************** 
    void deleteEXTTWHRecord(){ 
       DBAction action = database.table("EXTTWH").index("00").build()
       DBContainer EXTTWH = action.getContainer()
       EXTTWH.set("EXCONO", inCONO) 
       EXTTWH.set("EXSUNO", inSUNO)
       EXTTWH.set("EXTRCK", inTRCK)
       EXTTWH.set("EXFRDT", inFRDT)
       EXTTWH.set("EXTODT", inTODT)
       action.readLock(EXTTWH, deleterCallbackEXTTWH)
    }
      
    Closure<?> deleterCallbackEXTTWH = { LockedResult lockedResult ->  
       lockedResult.delete()
    }
    

 }