// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to update a supplier truck in EXTSTR
// Transaction UpdSupTruck
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SUNO - Supplier
 * @param: TRCK - Truck
 * @param: TRNA - Name
 * 
*/


public class UpdSupTruck extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database
    private final ProgramAPI program
    private final UtilityAPI utility
    private final LoggerAPI logger
    
    int inCONO
    String inSUNO
    String inTRCK
    String inTRNA
    
    // Constructor 
    public UpdSupTruck(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
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
        
       // Name
       if (mi.in.get("TRNA") != null && mi.in.get("TRNA") != "") {
          inTRNA = mi.inData.get("TRNA").trim() 
       } else {
          inTRNA = ""        
       }
  
       // Validate supplier truck record
       Optional<DBContainer> EXTSTR = findEXTSTR(inCONO, inSUNO, inTRCK)
       if(!EXTSTR.isPresent()){
          mi.error("Supplier Truck doesn't exist")   
          return             
       }     
      
       // Update record
       updEXTSTRRecord()
       
    }
    
      
    //******************************************************************** 
    // Get EXTSTR record
    //******************************************************************** 
    private Optional<DBContainer> findEXTSTR(int cono, String suno, String trck){  
       DBAction query = database.table("EXTSTR").index("00").build()
       def EXTSTR = query.getContainer()
       EXTSTR.set("EXCONO", cono)
       EXTSTR.set("EXSUNO", suno)
       EXTSTR.set("EXTRCK", trck)
       if(query.read(EXTSTR))  { 
         return Optional.of(EXTSTR)
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
    // Update EXTSTR record
    //********************************************************************    
    void updEXTSTRRecord(){      
       DBAction action = database.table("EXTSTR").index("00").build()
       DBContainer EXTSTR = action.getContainer()      
       EXTSTR.set("EXCONO", inCONO)     
       EXTSTR.set("EXSUNO", inSUNO)
       EXTSTR.set("EXTRCK", inTRCK)
  
       // Read with lock
       action.readLock(EXTSTR, updateCallBackEXTSTR)
       }
     
       Closure<?> updateCallBackEXTSTR = { LockedResult lockedResult -> 
       
       if (inTRNA == "?") {
          lockedResult.set("EXTRNA", "")
       } else {
          if (inTRNA != "") {
             lockedResult.set("EXTRNA", inTRNA)
          }
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

