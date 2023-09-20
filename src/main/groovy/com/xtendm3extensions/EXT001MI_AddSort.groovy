// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add a sort to EXTSOR
// Transaction AddSort
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SORT - Sort Code
 * @param: SONA - Name
 * @param: ITNO - Item Number
 * 
*/


public class AddSort extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  // Constructor 
  public AddSort(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.utility = utility
     this.program = program
     this.logger = logger
  } 
    
  public void main() {       
     // Set Company Number
     int CONO = program.LDAZD.CONO as Integer

     // Sort Code
     String inSORT
     if (mi.in.get("SORT") != null) {
        inSORT = mi.in.get("SORT") 
     } else {
        inSORT = ""         
     }
      
     // Name
     String inSONA
     if (mi.in.get("SONA") != null) {
        inSONA = mi.in.get("SONA") 
     } else {
        inSONA = ""        
     }
     
    // Item Number
     String inITNO
     if (mi.in.get("ITNO") != null) {
        inITNO = mi.in.get("ITNO") 
     } else {
        inITNO = ""        
     }

     // Validate sort record
     Optional<DBContainer> EXTSOR = findEXTSOR(CONO, inSORT)
     if(EXTSOR.isPresent()){
        mi.error("Sort already exists")   
        return             
     } else {
        // Write record 
        addEXTSORRecord(CONO, inSORT, inSONA, inITNO)          
     }  

  }
  
  //******************************************************************** 
  // Get EXTSOR record
  //******************************************************************** 
  private Optional<DBContainer> findEXTSOR(int CONO, String SORT){  
     DBAction query = database.table("EXTSOR").index("00").build()
     def EXTSOR = query.getContainer()
     EXTSOR.set("EXCONO", CONO)
     EXTSOR.set("EXSORT", SORT)
     if(query.read(EXTSOR))  { 
       return Optional.of(EXTSOR)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTSOR record 
  //********************************************************************     
  void addEXTSORRecord(int CONO, String SORT, String SONA, String ITNO){     
       DBAction action = database.table("EXTSOR").index("00").build()
       DBContainer EXTSOR = action.createContainer()
       EXTSOR.set("EXCONO", CONO)
       EXTSOR.set("EXSORT", SORT)
       EXTSOR.set("EXSONA", SONA)
       EXTSOR.set("EXITNO", ITNO)
   
       EXTSOR.set("EXCHID", program.getUser())
       EXTSOR.set("EXCHNO", 1) 
          
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTSOR.set("EXRGDT", regdate) 
       EXTSOR.set("EXLMDT", regdate) 
       EXTSOR.set("EXRGTM", regtime)
       action.insert(EXTSOR)         
 } 

     
} 

