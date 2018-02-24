import json
def traverse(data,value,categoryPath):
    flag=False
    if "name" in data:
        categoryPath.add(data["name"])
        if data["name"]==value:
            return True
    if "fb_page_categories" in data:
        for kid in data['fb_page_categories']:
            flag=traverse(kid,value,categoryPath)
            if flag:
                return True
    if "name" in data:
        categoryPath.discard(data["name"])
    return False
    
def getCategoryPath(categoryName):
    with open('/home/awitoomer123/stroller/calcRoute/facebookCategories.json') as json_data:
        categories = json.load(json_data)
    categoryPath=set()
    traverse(categories,categoryName,categoryPath)
    return categoryPath
