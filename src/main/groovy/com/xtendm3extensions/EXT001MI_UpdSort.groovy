// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to update a sort in EXTSOR
// Transaction UpdSort
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SORT - Sort Code
 * @param: SONA - Name
 * @param: ITNO - Item Number
 * 
*/


public class UpdSort extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database
    private final ProgramAPI program
    private final UtilityAPI utility
    private final LoggerAPI logger
    
    int inCONO
    String inSORT
    String inITNO
    String inSONA
  
    
    // Constructor 
    public UpdSort(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
       this.mi = mi
       this.database = database
       this.utility = utility
       this.program = program
       this.logger = logger     
    } 
      
    public void main() {       
       // Set Company Number
       inCONO = program.LDAZD.CONO as Integer
  
       // Sort Code
       if (mi.in.get("SORT") != null && mi.in.get("SORT") != "") {
          inSORT = mi.inData.get("SORT").trim() 
       } else {
          inSORT = ""         
       }
              
       // Item Number
       if (mi.in.get("ITNO") != null && mi.in.get("ITNO") != "") {
          inITNO = mi.inData.get("ITNO").trim()    
          
          // Validate item if entered
          Optional<DBContainer> MITMAS = findMITMAS(inCONO, inITNO)
          if (!MITMAS.isPresent()) {
            mi.error("Item Number doesn't exist")   
            return             
          }

       } else {
          inITNO = ""        
       }
       
       // Name
       if (mi.in.get("SONA") != null && mi.in.get("SONA") != "") {
          inSONA = mi.inData.get("SONA").trim() 
       } else {
          inSONA = ""        
       }
  
  
       // Validate sort record
       Optional<DBContainer> EXTSOR = findEXTSOR(inCONO, inSORT)
       if(!EXTSOR.isPresent()){
          mi.error("Sort doesn't exist")   
          return             
       }     
      
       // Update record
       updEXTSORRecord()
       
    }
    
      
    //******************************************************************** 
    // Get EXTSOR record
    //******************************************************************** 
    private Optional<DBContainer> findEXTSOR(int cono, String sort){  
       DBAction query = database.table("EXTSOR").index("00").build()
       DBContainer EXTSOR = query.getContainer()
       EXTSOR.set("EXCONO", cono)
       EXTSOR.set("EXSORT", sort)
       if(query.read(EXTSOR))  { 
         return Optional.of(EXTSOR)
       } 
    
       return Optional.empty()
    }
    

    //******************************************************************** 
    // Check Item
    //******************************************************************** 
    private Optional<DBContainer> findMITMAS(int CONO, String ITNO){  
        DBAction query = database.table("MITMAS").index("00").build()   
        DBContainer MITMAS = query.getContainer()
        MITMAS.set("MMCONO", CONO)
        MITMAS.set("MMITNO", ITNO)
    
        if(query.read(MITMAS))  { 
          return Optional.of(MITMAS)
        } 
  
        return Optional.empty()
    }


    //******************************************************************** 
    // Update EXTSOR record
    //********************************************************************    
    void updEXTSORRecord(){      
       DBAction action = database.table("EXTSOR").index("00").build()
       DBContainer EXTSOR = action.getContainer()       
       EXTSOR.set("EXCONO", inCONO)     
       EXTSOR.set("EXSORT", inSORT)
  
       // Read with lock
       action.readLock(EXTSOR, updateCallBackEXTSOR)
       }
     
       Closure<?> updateCallBackEXTSOR = { LockedResult lockedResult -> 
  
       if (inITNO == "?") {
          lockedResult.set("EXITNO", "")
       } else {
          if (inITNO != "") {
             lockedResult.set("EXITNO", inITNO)
          }
       }
  
       if (inSONA == "?") {
          lockedResult.set("EXSONA", "")
       } else {     
          if (inSONA != "") {
            lockedResult.set("EXSONA", inSONA)
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

