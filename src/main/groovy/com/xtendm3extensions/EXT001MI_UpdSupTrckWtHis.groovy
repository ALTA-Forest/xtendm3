// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to update a supplier truck in EXTTWH
// Transaction UpdSupTrckWtHis
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SUNO - Supplier
 * @param: TRCK - Truck
 * @param: TARE - Tare
 * @param: FRDT - From Date
 * @param: TODT - To Date
 * 
*/


public class UpdSupTrckWtHis extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database
    private final ProgramAPI program
    private final UtilityAPI utility
    private final LoggerAPI logger
    
    int inCONO
    String inSUNO
    String inTRCK
    int inTARE
    int inFRDT
    int inTODT
    
    
    // Constructor 
    public UpdSupTrckWtHis(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
       this.mi = mi
       this.database = database
       this.utility = utility
       this.program = program
       this.logger = logger     
    } 
      
    public void main() {       
       // Set Company Number
       inCONO = program.LDAZD.CONO as Integer
  
       // Supplier
       if (mi.in.get("SUNO") != null && mi.in.get("SUNO") != "") {
          inSUNO = mi.inData.get("SUNO").trim() 
          
          // Validate supplier if entered
          Optional<DBContainer> CIDMAS = findCIDMAS(inCONO, inSUNO)
          if (!CIDMAS.isPresent()) {
            mi.error("Supplier doesn't exist")   
            return             
          }

       } else {
          inSUNO = ""         
       }
        
       // Truck
       if (mi.in.get("TRCK") != null && mi.in.get("TRCK") != "") {
          inTRCK = mi.inData.get("TRCK").trim() 
       } else {
          inTRCK = ""        
       }
  
       // Tare
       if (mi.in.get("TARE") != null) {
          inTARE = mi.in.get("TARE") 
       } 
  
       // From Date
       if (mi.in.get("FRDT") != null) {
          inFRDT = mi.in.get("FRDT") 
          
          //Validate date format
          boolean validFRDT = utility.call("DateUtil", "isDateValid", String.valueOf(inFRDT), "yyyyMMdd")  
          if (!validFRDT) {
            mi.error("From Date is not valid")   
            return  
          }  

       }
  
       // To Date
       if (mi.in.get("TODT") != null) {
          inTODT = mi.in.get("TODT") 
          
          //Validate date format
          boolean validTODT = utility.call("DateUtil", "isDateValid", String.valueOf(inTODT), "yyyyMMdd")  
          if (!validTODT) {
            mi.error("From Date is not valid")   
            return  
          } 

       } 
       
  
       // Validate supplier truck record
       Optional<DBContainer> EXTTWH = findEXTTWH(inCONO, inSUNO, inTRCK, inFRDT, inTODT)
       if(!EXTTWH.isPresent()){
          mi.error("Supplier Truck Weight History doesn't exist")   
          return             
       }     
      
       // Update record
       updEXTTWHRecord()
       
    }
    
     
    //******************************************************************** 
    // Get EXTTWH record
    //******************************************************************** 
    private Optional<DBContainer> findEXTTWH(int cono, String suno, String trck, int frdt, int todt){  
       DBAction query = database.table("EXTTWH").index("00").selection("EXFRDT", "EXTODT").build()
       DBContainer EXTTWH = query.getContainer()
       EXTTWH.set("EXCONO", cono)
       EXTTWH.set("EXSUNO", suno)
       EXTTWH.set("EXTRCK", trck)
       EXTTWH.set("EXFRDT", frdt)
       EXTTWH.set("EXTODT", todt)
       if(query.read(EXTTWH))  { 
         return Optional.of(EXTTWH)
       } 
    
       return Optional.empty()
    }


    //******************************************************************** 
    // Check Supplier
    //******************************************************************** 
    private Optional<DBContainer> findCIDMAS(int CONO, String SUNO){  
      DBAction query = database.table("CIDMAS").index("00").build()   
      DBContainer CIDMAS = query.getContainer()
      CIDMAS.set("IDCONO", CONO)
      CIDMAS.set("IDSUNO", SUNO)
    
      if(query.read(CIDMAS))  { 
        return Optional.of(CIDMAS)
      } 
  
      return Optional.empty()
    }

  
    //******************************************************************** 
    // Update EXTTWH record
    //********************************************************************    
    void updEXTTWHRecord(){      
       DBAction action = database.table("EXTTWH").index("00").build()
       DBContainer EXTTWH = action.getContainer()       
       EXTTWH.set("EXCONO", inCONO)     
       EXTTWH.set("EXSUNO", inSUNO)
       EXTTWH.set("EXTRCK", inTRCK)
       EXTTWH.set("EXFRDT", inFRDT)
       EXTTWH.set("EXTODT", inTODT)
  
       // Read with lock
       action.readLock(EXTTWH, updateCallBackEXTTWH)
       }
     
       Closure<?> updateCallBackEXTTWH = { LockedResult lockedResult -> 
       
       if (mi.in.get("TARE") != null) {
          lockedResult.set("EXTARE", inTARE)
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

